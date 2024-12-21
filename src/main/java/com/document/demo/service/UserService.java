package com.document.demo.service;

import com.document.demo.dto.request.UserRegistrationRequest;
import com.document.demo.models.User;
import com.document.demo.models.enums.UserRole;
import com.document.demo.models.enums.UserStatus;
import com.document.demo.dto.request.ChangePasswordRequest;
import com.document.demo.dto.request.UpdateProfileRequest;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    User registerUser(UserRegistrationRequest request);
    User findByUsername(String username);
    User findByEmail(String email);
    List<User> findByDepartment(String departmentId);
    List<User> findByRole(UserRole role);
    void changePassword(String userId, ChangePasswordRequest request);
    void updateProfile(String userId, UpdateProfileRequest request) throws FileUploadException;
    void updateStatus(String userId, UserStatus status);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    User getUserById(String id);

}