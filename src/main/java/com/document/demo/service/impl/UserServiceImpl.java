package com.document.demo.service.impl;

import com.document.demo.dto.request.ChangePasswordRequest;
import com.document.demo.dto.request.TrackingRequest;
import com.document.demo.dto.request.UpdateProfileRequest;
import com.document.demo.dto.request.UserRegistrationRequest;
import com.document.demo.exception.InvalidPasswordException;
import com.document.demo.exception.ResourceAlreadyExistsException;
import com.document.demo.exception.ResourceNotFoundException;
import com.document.demo.models.Department;
import com.document.demo.models.User;
import com.document.demo.models.enums.TrackingActionType;
import com.document.demo.models.enums.TrackingEntityType;
import com.document.demo.models.enums.UserRole;
import com.document.demo.models.enums.UserStatus;
import com.document.demo.models.tracking.ChangeLog;
import com.document.demo.repository.DepartmentRepository;
import com.document.demo.repository.UserRepository;
import com.document.demo.service.S3Service;
import com.document.demo.service.TrackingService;
import com.document.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.document.demo.utils.UpdateFieldUtils.updateField;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final S3Service s3Service;
    private final UserRepository userRepository;
    private final TrackingService trackingService;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public User registerUser(UserRegistrationRequest user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new ResourceAlreadyExistsException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User newUser = User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .status(UserStatus.INACTIVE)
            .build();

        User savedUser = userRepository.save(newUser);
        
        // Track user creation
        trackingService.track(TrackingRequest.builder()
            .actor(savedUser)
            .entityType(TrackingEntityType.USER)
            .entityId(savedUser.getUserId())
            .action(TrackingActionType.CREATE)
            .metadata(Map.of(
                "username", savedUser.getUsername(),
                "email", savedUser.getEmail(),
                "role", savedUser.getRole().toString()
            ))
            .build());
            
        return savedUser;
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        User user = getUserById(id);
        
        // Track user deletion
        trackingService.track(TrackingRequest.builder()
            .actor(getCurrentUser())
            .entityType(TrackingEntityType.USER)
            .entityId(id)
            .action(TrackingActionType.DELETE)
            .metadata(Map.of(
                "username", user.getUsername(),
                "email", user.getEmail()
            ))
            .build());
            
        userRepository.delete(user);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    public List<User> findByDepartment(String departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));
        return userRepository.findByDepartment(department);
    }

    @Override
    public List<User> findByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    @Override
    @Transactional
    public void changePassword(String userId, ChangePasswordRequest request, boolean isForgot) {
        User user = getUserById(userId);

        if(!isForgot){
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new InvalidPasswordException("Current password is incorrect");
            }
        }
        
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidPasswordException("New password and confirm password do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        Map<String, ChangeLog> changes = new HashMap<>();
        changes.put("password", new ChangeLog());
        trackingService.track(TrackingRequest.builder()
            .actor(isForgot? user : getCurrentUser())
            .entityType(TrackingEntityType.USER)
            .entityId(userId)
            .action(TrackingActionType.PASSWORD_CHANGE)
            .changes(changes)
            .build());

        userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateProfile(String userId, UpdateProfileRequest request) {
        User user = getUserById(userId);

        Map<String, ChangeLog> changes = new HashMap<>();
        updateField(changes, "username", user.getUsername(), request.getUserName(), user::setUsername);
        updateField(changes, "fullName", user.getFullName(), request.getFullName(), user::setFullName);
        updateField(changes, "email", user.getEmail(), request.getEmail(), user::setEmail);
        updateField(changes, "phone", user.getPhone(), request.getPhone(), user::setPhone);
        updateField(changes, "position", user.getPosition(), request.getPosition(), user::setPosition);

        if(request.getAvatarFile() != null && !request.getAvatarFile().isEmpty()) {
            changes.put("avatar", new ChangeLog());
            handleImageUpdate(request, user.getAvatar(), user::setAvatar);
        }
        if (request.getBackgroundFile() != null && !request.getBackgroundFile().isEmpty()) {
            changes.put("background", new ChangeLog());
            handleImageUpdate(request, user.getBackground(), user::setBackground);
        }

        User savedUser = userRepository.save(user);

        // Track user update
        trackingService.track(TrackingRequest.builder()
            .actor(getCurrentUser())
            .entityType(TrackingEntityType.USER)
            .entityId(userId)
            .action(TrackingActionType.UPDATE)
            .changes(changes)
            .metadata(Map.of("id", savedUser.getUserId()))
            .build());

        return savedUser;
    }

    private void handleImageUpdate(UpdateProfileRequest request, String currentImagePath, Consumer<String> setter) {
        // Delete existing image if exists
        if (currentImagePath != null) {
            s3Service.deleteFile(currentImagePath);
        }

        // Store new image and get file path
        String newImagePath = s3Service.uploadFile(request.getAvatarFile());
        setter.accept(newImagePath);
    }

    @Override
    @Transactional
    public void updateStatus(String userId, UserStatus status) {
        User user = getUserById(userId);
        Map<String, ChangeLog> changes = new HashMap<>();
        updateField(changes, "status", user.getStatus(), status, user::setStatus);

        User savedUser = userRepository.save(user);

        // Track user status update
        trackingService.track(TrackingRequest.builder()
            .actor(getCurrentUser())
            .entityType(TrackingEntityType.USER)
            .entityId(userId)
            .action(TrackingActionType.UPDATE)
            .changes(changes)
            .metadata(Map.of("idUser", savedUser.getUserId()))
            .build());
    }

    @Override
    @Transactional
    public void updateRole(String userId, UserRole role) {
        User user = getUserById(userId);

        Map<String, ChangeLog> changes = new HashMap<>();
        updateField(changes, "role", user.getRole(), role, user::setRole);

        User savedUser = userRepository.save(user);

        // Track user role update
        trackingService.track(TrackingRequest.builder()
            .actor(getCurrentUser())
            .entityType(TrackingEntityType.USER)
            .entityId(userId)
            .action(TrackingActionType.UPDATE)
            .changes(changes)
            .metadata(Map.of("idUser", savedUser.getUserId()))
            .build());
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String username = authentication.getName();
        return findByUsername(username);
    }

    @Override
    public Page<User> findAll(int page, int size, String search, UserRole role, UserStatus status) {
        Pageable pageable = PageRequest.of(page, size);
        
        if (search != null && !search.isEmpty()) {
            return userRepository.searchUsers(search, pageable);
        }
        
        if (role != null && status != null) {
            return userRepository.findByRoleAndStatus(role, status, pageable);
        }
        
        if (role != null) {
            return userRepository.findByRole(role, pageable);
        }
        
        if (status != null) {
            return userRepository.findByStatus(status, pageable);
        }
        
        return userRepository.findAll(pageable);
    }
}
