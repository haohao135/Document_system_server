package com.document.demo.service;

import com.document.demo.models.Department;

import java.util.List;

public interface DepartmentService {
    Department createDepartment(Department department);
    Department updateDepartment(String id, Department department);
    void deleteDepartment(String id);
    Department findById(String id);
    Department findByName(String name);
    List<Department> findAll();
    boolean existsByName(String name);
} 