package com.document.demo.controller;

import com.document.demo.dto.request.DepartmentRequest;
import com.document.demo.dto.request.PositionRequest;
import com.document.demo.dto.response.ErrorResponse;
import com.document.demo.dto.response.SuccessResponse;
import com.document.demo.exception.ResourceAlreadyExistsException;
import com.document.demo.exception.ResourceNotFoundException;
import com.document.demo.models.Department;
import com.document.demo.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentService departmentService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createDepartment(
            @Valid @RequestBody DepartmentRequest request) {
        try {
            Department department = Department.builder()
                .name(request.getName())
                .hotline(request.getHotline())
                .description(request.getDescription())
                .location(request.getLocation())
                .build();

            Department savedDepartment = departmentService.createDepartment(department);

            return ResponseEntity.ok(new SuccessResponse(
                "Department created successfully",
                savedDepartment
            ));

        } catch (ResourceAlreadyExistsException e) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error creating department: " + e.getMessage()));
        }
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateDepartment(
            @PathVariable String id,
            @Valid @RequestBody DepartmentRequest request) {
        try {
            Department department = Department.builder()
                .departmentId(id)
                .name(request.getName())
                .hotline(request.getHotline())
                .description(request.getDescription())
                .location(request.getLocation())
                .build();

            Department updatedDepartment = departmentService.updateDepartment(department);

            return ResponseEntity.ok(new SuccessResponse(
                "Department updated successfully",
                updatedDepartment
            ));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error updating department: " + e.getMessage()));
        }
    }

    @PostMapping("/add-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addUserToDepartment(
            @RequestParam String id,
            @RequestParam String userId,
            @Valid @RequestBody PositionRequest request) {
        try {
            Department updatedDepartment = departmentService.addUserToDepartment(id, userId, request);

            return ResponseEntity.ok(new SuccessResponse(
                "User added to department successfully",
                updatedDepartment
            ));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error adding user to department: " + e.getMessage()));
        }
    }

    @PostMapping("/remove-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeUserFromDepartment(
            @RequestParam String id,
            @RequestParam String userId) {
        try {
            Department updatedDepartment = departmentService.removeUserFromDepartment(id, userId);

            return ResponseEntity.ok(new SuccessResponse(
                "User removed from department successfully",
                updatedDepartment
            ));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error removing user from department: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getDepartmentById(@PathVariable String id) {
        try {
            Department department = departmentService.findById(id);
            return ResponseEntity.ok(new SuccessResponse("Department retrieved successfully", department));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error retrieving department: " + e.getMessage()));
        }
    }
} 