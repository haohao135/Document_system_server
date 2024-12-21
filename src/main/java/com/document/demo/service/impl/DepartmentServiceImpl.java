package com.document.demo.service.impl;

import com.document.demo.exception.ResourceAlreadyExistsException;
import com.document.demo.exception.ResourceNotFoundException;
import com.document.demo.models.Department;
import com.document.demo.repository.DepartmentRepository;
import com.document.demo.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public Department createDepartment(Department department) {
        if (existsByName(department.getName())) {
            throw new ResourceAlreadyExistsException("Department name already exists");
        }
        return departmentRepository.save(department);
    }

    @Override
    @Transactional
    public Department updateDepartment(String id, Department department) {
        Department existingDepartment = findById(id);

        if (department.getName() != null && 
            !department.getName().equals(existingDepartment.getName()) && 
            existsByName(department.getName())) {
            throw new ResourceAlreadyExistsException("Department name already exists");
        }

        if (department.getName() != null) {
            existingDepartment.setName(department.getName());
        }
        if (department.getDescription() != null) {
            existingDepartment.setDescription(department.getDescription());
        }

        return departmentRepository.save(existingDepartment);
    }

    @Override
    @Transactional
    public void deleteDepartment(String id) {
        Department department = findById(id);
        if (!department.getUsers().isEmpty()) {
            throw new IllegalStateException("Cannot delete department with assigned users");
        }
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