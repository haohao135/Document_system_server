package com.document.demo.service;

import com.document.demo.dto.request.UserRegistrationRequest;
import com.document.demo.models.User;
import com.document.demo.models.enums.UserRole;
import com.document.demo.models.enums.UserStatus;
import com.document.demo.dto.request.ChangePasswordRequest;
import com.document.demo.dto.request.UpdateProfileRequest;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    User registerUser(UserRegistrationRequest request);
    void deleteUser(String userId);

    User findByUsername(String username);
    User findByEmail(String email);
    List<User> findByDepartment(String departmentId);
    List<User> findByRole(UserRole role);
    void changePassword(String userId, ChangePasswordRequest request, boolean isForgot);
    void updateProfile(String userId, UpdateProfileRequest request) throws FileUploadException;
    void updateStatus(String userId, UserStatus status);
    void updateRole(String userId, UserRole role);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    User getUserById(String id);

    User getCurrentUser();

    Page<User> findAll(int page, int size, String search, UserRole role, UserStatus status);
}