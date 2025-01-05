package com.document.demo.service.impl;

import com.document.demo.dto.request.TrackingRequest;
import com.document.demo.exception.ResourceAlreadyExistsException;
import com.document.demo.exception.ResourceNotFoundException;
import com.document.demo.exception.UserAlreadyExistsException;
import com.document.demo.models.Department;
import com.document.demo.models.User;
import com.document.demo.models.enums.TrackingActionType;
import com.document.demo.models.enums.TrackingEntityType;
import com.document.demo.models.tracking.ChangeLog;
import com.document.demo.repository.DepartmentRepository;
import com.document.demo.repository.UserRepository;
import com.document.demo.service.DepartmentService;
import com.document.demo.service.TrackingService;
import com.document.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static com.document.demo.utils.UpdateFieldUtils.updateField;

@Service
@Slf4j
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final TrackingService trackingService;
    private final UserService userService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Department createDepartment(Department department) {
        // Validate department name uniqueness
        if (departmentRepository.existsByName(department.getName())) {
            throw new ResourceAlreadyExistsException("Department with name '" + department.getName() + "' already exists");
        }

        Department savedDepartment = departmentRepository.save(department);
        
        // Track creation
        trackingService.track(TrackingRequest.builder()
            .actor(userService.getCurrentUser())
            .entityType(TrackingEntityType.DEPARTMENT)
            .entityId(savedDepartment.getDepartmentId())
            .action(TrackingActionType.CREATE)
            .description("Created new department: " + savedDepartment.getName())
            .metadata(Map.of(
                "name", savedDepartment.getName(),
                "hotline", savedDepartment.getHotline(),
                "location", savedDepartment.getLocation()
            ))
            .build());
            
        return savedDepartment;
    }

    @Override
    @Transactional
    public Department updateDepartment(Department department) {
        Department existingDepartment = findById(department.getDepartmentId());
        
        // Check name uniqueness if name is being changed
        if (!existingDepartment.getName().equals(department.getName()) && 
            departmentRepository.existsByName(department.getName())) {
            throw new ResourceAlreadyExistsException("Department with name '" + department.getName() + "' already exists");
        }

        // Track changes
        Map<String, ChangeLog> changes = new HashMap<>();
        updateField(changes, "name", existingDepartment.getName(), department.getName(), existingDepartment::setName);
        updateField(changes, "hotline", existingDepartment.getHotline(), department.getHotline(), existingDepartment::setHotline);
        updateField(changes, "description", existingDepartment.getDescription(), department.getDescription(), existingDepartment::setDescription);
        updateField(changes, "location", existingDepartment.getLocation(), department.getLocation(), existingDepartment::setLocation);

        // Only save if there are changes
        Department updatedDepartment = existingDepartment;
        if (!changes.isEmpty()) {
            updatedDepartment = departmentRepository.save(existingDepartment);
            
            // Track update
            trackingService.track(TrackingRequest.builder()
                .actor(userService.getCurrentUser())
                .entityType(TrackingEntityType.DEPARTMENT)
                .entityId(department.getDepartmentId())
                .action(TrackingActionType.UPDATE)
                .changes(changes)
                .build());
        }
            
        return updatedDepartment;
    }

    @Override
    @Transactional
    public void deleteDepartment(String id) {
        Department department = findById(id);
        
        // Check if department has users
        List<User> departmentUsers = userRepository.findByDepartment(department);
        if (!departmentUsers.isEmpty()) {
            throw new IllegalStateException("Cannot delete department that has users. Remove all users first.");
        }
        
        // Track deletion
        trackingService.track(TrackingRequest.builder()
            .actor(userService.getCurrentUser())
            .entityType(TrackingEntityType.DEPARTMENT)
            .entityId(id)
            .action(TrackingActionType.DELETE)
            .metadata(Map.of(
                "name", department.getName(),
                "hotline", department.getHotline(),
                "location", department.getLocation()
            ))
            .build());
            
        departmentRepository.delete(department);
    }

    @Override
    @Transactional
    public Department addUserToDepartment(String id, String userId) {
        Department department = findById(id);
        User user = userService.getUserById(userId);

        // Validate user's current department status
        if (user.getDepartment() != null) {
            if (user.getDepartment().equals(department)) {
                throw new UserAlreadyExistsException("User already belongs to this department");
            } else {
                throw new UserAlreadyExistsException("User already belongs to another department: " 
                    + user.getDepartment().getName());
            }
        }

        // Add user to department
        user.setDepartment(department);
        userRepository.save(user);

        // Track user addition
        trackingService.track(TrackingRequest.builder()
            .actor(userService.getCurrentUser())
            .entityType(TrackingEntityType.DEPARTMENT)
            .entityId(department.getDepartmentId())
            .action(TrackingActionType.ADD_USER)
            .description("Added user to department: " + user.getFullName())
            .metadata(Map.of(
                "userId", user.getUserId(),
                "username", user.getUsername(),
                "userEmail", user.getEmail()
            ))
            .build());

        return department;
    }

    @Override
    @Transactional
    public Department removeUserFromDepartment(String id, String userId) {
        Department department = findById(id);
        User user = userService.getUserById(userId);

        // Validate user belongs to this department
        if (user.getDepartment() == null || !user.getDepartment().equals(department)) {
            throw new ResourceNotFoundException("User does not belong to this department");
        }

        // Remove user from department
        user.setDepartment(null);
        userRepository.save(user);

        // Track user removal
        trackingService.track(TrackingRequest.builder()
            .actor(userService.getCurrentUser())
            .entityType(TrackingEntityType.DEPARTMENT)
            .entityId(department.getDepartmentId())
            .action(TrackingActionType.REMOVE_USER)
            .description("Removed user from department: " + user.getFullName())
            .metadata(Map.of(
                "userId", user.getUserId(),
                "username", user.getUsername(),
                "userEmail", user.getEmail()
            ))
            .build());

        return department;
    }

    @Override
    public Department findById(String id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    @Override
    public Department findByName(String name) {
        return departmentRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with name: " + name));
    }

    @Override
    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    @Override
    public boolean existsByName(String name) {
        return departmentRepository.existsByName(name);
    }
} 