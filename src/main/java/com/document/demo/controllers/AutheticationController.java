package com.document.demo.controllers;

import com.document.demo.dto.request.UserRegistrationRequest;
import com.document.demo.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AutheticationController {
    @Autowired
    UserService userService;
    @GetMapping("/hello")
    public String hello(){
        return "hello world";
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserRegistrationRequest request) {
        String response = userService.registerUser(request);
        if (response.equals("Username already exists!") || response.equals("Passwords do not match!") || response.equals("Email already registered!")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}
