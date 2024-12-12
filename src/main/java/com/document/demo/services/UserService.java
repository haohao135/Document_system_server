package com.document.demo.services;

import com.document.demo.dto.request.UserRegistrationRequest;
import com.document.demo.models.User;
import com.document.demo.models.enums.UserStatus;
import com.document.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public String registerUser(UserRegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return "Username already exists!";
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return "Passwords do not match!";
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return "Email already registered!";
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setStatus(UserStatus.INACTIVE.name());
        userRepository.save(user);
        return "User registered successfully!";
    }
}
