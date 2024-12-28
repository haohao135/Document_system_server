package com.document.demo.service.impl;

import com.document.demo.dto.request.TrackingRequest;
import com.document.demo.exception.ResourceAlreadyExistsException;
import com.document.demo.exception.ResourceNotFoundException;
import com.document.demo.models.Department;
import com.document.demo.models.enums.TrackingActionType;
import com.document.demo.models.enums.TrackingEntityType;
import com.document.demo.models.tracking.ChangeLog;
import com.document.demo.repository.DepartmentRepository;
import com.document.demo.service.DepartmentService;
import com.document.demo.service.TrackingService;
import com.document.demo.utils.SecurityUtils;
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
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public Department createDepartment(Department department) {
        if (departmentRepository.existsByName(department.getName())) {
            throw new ResourceAlreadyExistsException("Department with this name already exists");
        }

        Department savedDepartment = departmentRepository.save(department);
        
        // Track department creation
        trackingService.track(TrackingRequest.builder()
            .actor(securityUtils.getCurrentUser())
            .entityType(TrackingEntityType.DEPARTMENT)
            .entityId(savedDepartment.getDepartmentId())
            .action(TrackingActionType.CREATE)
            .metadata(Map.of(
                "name", savedDepartment.getName(),
                "description", savedDepartment.getDescription()
            ))
            .build());
            
        return savedDepartment;
    }

    @Override
    @Transactional
    public Department updateDepartment(String id, Department department) {
        Department existingDepartment = findById(id);
        
        if (department.getName() != null && 
            !department.getName().equals(existingDepartment.getName()) && 
            departmentRepository.existsByName(department.getName())) {
            throw new ResourceAlreadyExistsException("Department with this name already exists");
        }

        Map<String, ChangeLog> changes = new HashMap<>();
        updateField(changes, "name", existingDepartment.getName(), department.getName(), existingDepartment::setName);
        updateField(changes, "description", existingDepartment.getDescription(), department.getDescription(), existingDepartment::setDescription);

        Department updatedDepartment = departmentRepository.save(existingDepartment);
        
        // Track department update
        trackingService.track(TrackingRequest.builder()
            .actor(securityUtils.getCurrentUser())
            .entityType(TrackingEntityType.DEPARTMENT)
            .entityId(id)
            .action(TrackingActionType.UPDATE)
            .changes(changes)
            .build());
            
        return updatedDepartment;
    }

    @Override
    @Transactional
    public void deleteDepartment(String id) {
        Department department = findById(id);
        
        // Track department deletion
        trackingService.track(TrackingRequest.builder()
            .actor(securityUtils.getCurrentUser())
            .entityType(TrackingEntityType.DEPARTMENT)
            .entityId(id)
            .action(TrackingActionType.DELETE)
            .metadata(Map.of(
                "name", department.getName(),
                "description", department.getDescription()
            ))
            .build());
            
        departmentRepository.delete(department);
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