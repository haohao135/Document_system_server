package com.document.demo.controller;

import com.document.demo.dto.request.EmailRequest;
import com.document.demo.dto.request.LoginRequest;
import com.document.demo.dto.request.RefreshTokenRequest;
import com.document.demo.dto.request.UserRegistrationRequest;
import com.document.demo.dto.response.EmailResponse;
import com.document.demo.dto.response.ErrorResponse;
import com.document.demo.dto.response.JwtResponse;
import com.document.demo.dto.response.SuccessResponse;
import com.document.demo.exception.ResourceAlreadyExistsException;
import com.document.demo.models.User;
import com.document.demo.models.enums.UserStatus;
import com.document.demo.security.JwtTokenProvider;
import com.document.demo.service.EmailService;
import com.document.demo.service.OtpService;
import com.document.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final EmailService emailService;
    private final OtpService otpService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authentication);

            User user = (User) authentication.getPrincipal();
            return ResponseEntity.ok(new JwtResponse(
                jwt,
                refreshToken,
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
            ));
        } catch (Exception e) {
            log.error("Authentication failed", e);
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Invalid username or password"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationRequest request) {
        try {
            User user = userService.registerUser(request);
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new SuccessResponse("User registered successfully", user));
        } catch (ResourceAlreadyExistsException e) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            String token = request.getRefreshToken();
            if (!tokenProvider.validateToken(token)) {
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid refresh token"));
            }

            String username = tokenProvider.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities()
            );
            
            String newToken = tokenProvider.generateToken(authentication);
            String newRefreshToken = tokenProvider.generateRefreshToken(authentication);

            return ResponseEntity.ok(new JwtResponse(
                newToken,
                newRefreshToken,
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
            ));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Could not refresh token"));
        }
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendEmail(@RequestBody EmailRequest request) {
        User user = userService.findByEmail(request.getTo());
        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid email"));
        }
        if(user.getStatus().equals(UserStatus.INACTIVE)){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Account is inactive"));
        }
        
        // Generate OTP
        String otp = otpService.generateOtp(6, true);
        // Save OTP to Redis
        otpService.saveOtp(request.getTo(), otp);

        // Send email with OTP
        EmailResponse emailResponse = emailService.sendOtpEmail(
            request.getTo(),
            otp
        );

        if (!emailResponse.isSuccess()) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(emailResponse.getMessage()));
        }

        return ResponseEntity.ok(new SuccessResponse("OTP sent successfully", emailResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new SuccessResponse("Logged out successfully"));
    }
}