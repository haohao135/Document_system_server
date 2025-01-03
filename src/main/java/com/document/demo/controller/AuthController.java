package com.document.demo.controller;

import com.document.demo.dto.request.*;
import com.document.demo.dto.response.EmailResponse;
import com.document.demo.dto.response.ErrorResponse;
import com.document.demo.dto.response.JwtResponse;
import com.document.demo.dto.response.SuccessResponse;
import com.document.demo.exception.ResourceAlreadyExistsException;
import com.document.demo.exception.ResourceNotFoundException;
import com.document.demo.models.User;
import com.document.demo.models.enums.TrackingActionType;
import com.document.demo.models.enums.TrackingEntityType;
import com.document.demo.models.enums.UserStatus;
import com.document.demo.security.JwtTokenProvider;
import com.document.demo.service.EmailService;
import com.document.demo.service.OtpService;
import com.document.demo.service.TrackingService;
import com.document.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.util.StringUtils;

import java.util.Map;

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
    private final TrackingService trackingService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        if (userService.findByUsername(request.getUsername()) == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Invalid username or password"));
        } else if (userService.findByUsername(request.getUsername()).getStatus().equals(UserStatus.INACTIVE)) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Account is inactive"));

        }

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

            // track login
            trackingService.track(
                    TrackingRequest.builder()
                            .actor(user)
                            .entityType(TrackingEntityType.USER)
                            .entityId(user.getUserId())
                            .action(TrackingActionType.LOGIN)
                            .build()
            );

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

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtpEmail(@Valid @RequestBody EmailRequest request) {
        try {
            User user = userService.findByEmail(request.getTo());
            
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

            trackingService.track(
                TrackingRequest.builder()
                    .actor(user)
                    .entityType(TrackingEntityType.USER)
                    .entityId(user.getUserId())
                    .action(TrackingActionType.SEND_OTP)
                    .build()
            );

            return ResponseEntity.ok(new SuccessResponse("OTP sent successfully", emailResponse));
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Email is not registered"));
        } catch (Exception e) {
            log.error("Error sending OTP: ", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to send OTP"));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        try {
            User user = userService.findByEmail(request.getEmail());
            if (user == null) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid email"));
            }

            // Verify OTP
            boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtp());
            
            if (!isValid) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid OTP or OTP has expired"));
            }

            // generate reset password token
            String resetPasswordToken = otpService.generateResetPasswordToken(request.getEmail());

            // Delete OTP after verify
            otpService.deleteOtp(request.getEmail());

            trackingService.track(
                TrackingRequest.builder()
                    .actor(user)
                    .entityType(TrackingEntityType.USER)
                    .entityId(user.getUserId())
                    .action(TrackingActionType.VERIFY_OTP)
                    .build()
            );

            return ResponseEntity.ok(new SuccessResponse("OTP verified successfully", Map.of("resetToken", resetPasswordToken)));
            
        } catch (Exception e) {
            log.error("Error verifying OTP: ", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to verify OTP: " + e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            // Validate reset token
            if (!otpService.validateResetPasswordToken(request.getEmail(), request.getResetToken())) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid or expired reset token"));
            }

            // Reset password
            User user = userService.findByEmail(request.getEmail());

            ChangePasswordRequest newPassword = ChangePasswordRequest.builder()
                .newPassword(request.getNewPassword())
                .confirmPassword(request.getConfirmPassword())
                .build();

            userService.changePassword(user.getUserId(), newPassword, true);
            
            // Delete reset token
            otpService.deleteResetPasswordToken(request.getEmail());

            return ResponseEntity.ok(new SuccessResponse("Password reset successfully"));
            
        } catch (Exception e) {
            log.error("Error resetting password: ", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to reset password: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            // Get tokens from request
            String accessToken = getJwtFromRequest(request);
            String refreshToken = request.getHeader("Refresh-Token");

            // Kiểm tra tokens có tồn tại không
            if (accessToken == null && refreshToken == null) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("No tokens provided"));
            }

            // Kiểm tra tokens có trong blacklist chưa
            if (accessToken != null && !tokenProvider.validateToken(accessToken)) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Token is already invalidated"));
            }

            if (refreshToken != null && !tokenProvider.validateToken(refreshToken, true)) {
                return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Refresh token is already invalidated"));
            }

            // Invalidate tokens nếu hợp lệ
            tokenProvider.invalidateTokens(accessToken, refreshToken);

            // Get current user for tracking
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User user) {
                trackingService.track(
                    TrackingRequest.builder()
                        .actor(user)
                        .entityType(TrackingEntityType.USER)
                        .entityId(user.getUserId())
                        .action(TrackingActionType.LOGOUT)
                        .build()
                );
            }

            // Clear security context
            SecurityContextHolder.clearContext();

            return ResponseEntity.ok(new SuccessResponse("Logged out successfully"));
            
        } catch (Exception e) {
            log.error("Error during logout: ", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to logout: " + e.getMessage()));
        }
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}