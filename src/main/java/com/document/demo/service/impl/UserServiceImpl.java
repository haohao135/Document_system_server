package com.document.demo.service.impl;

import com.document.demo.dto.request.ChangePasswordRequest;
import com.document.demo.dto.request.UpdateProfileRequest;
import com.document.demo.dto.request.UserRegistrationRequest;
import com.document.demo.exception.InvalidPasswordException;
import com.document.demo.exception.ResourceAlreadyExistsException;
import com.document.demo.exception.ResourceNotFoundException;
import com.document.demo.models.Department;
import com.document.demo.models.User;
import com.document.demo.models.enums.UserRole;
import com.document.demo.models.enums.UserStatus;
import com.document.demo.repository.UserRepository;
import com.document.demo.service.CloudinaryService;
import com.document.demo.service.DepartmentService;
import com.document.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final CloudinaryService cloudinaryService;
    private final DepartmentService departmentService;

    @Override
    public User registerUser(UserRegistrationRequest request) {
        if (existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("Username already exists");
        }
        if (existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new InvalidPasswordException("Password and confirm password do not match");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getUsername())
                .role(UserRole.USER)
                .status(UserStatus.INACTIVE)
                .build();

        return userRepository.save(user);
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
        Department department = departmentService.findById(departmentId);
        return userRepository.findByDepartment(department);
    }

    @Override
    public List<User> findByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    @Override
    @Transactional
    public void changePassword(String userId, ChangePasswordRequest request) {
        User user = getUserById(userId);
        
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }
        
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidPasswordException("New password and confirm password do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateProfile(String userId, UpdateProfileRequest request) throws FileUploadException {
        User user = getUserById(userId);

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPosition() != null) {
            user.setPosition(request.getPosition());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            String avatarUrl = cloudinaryService.uploadFile(request.getAvatarFile());
            user.setAvatar(avatarUrl);
        }
        if (request.getBackground() != null && !request.getBackground().isEmpty()) {
            String backgroundUrl = cloudinaryService.uploadFile(request.getBackgroundFile());
            user.setBackground(backgroundUrl);
        }

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateStatus(String userId, UserStatus status) {
        User user = getUserById(userId);
        user.setStatus(status);
        userRepository.save(user);
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
}
