package com.eeki.adminservice.controller;

import com.eeki.adminservice.dto.AuthResponse;
import com.eeki.adminservice.dto.EmailOtpRequest;
import com.eeki.adminservice.dto.LoginRequest;
import com.eeki.adminservice.dto.RegisterRequest;
import com.eeki.adminservice.dto.SendOtpRequest;
import com.eeki.adminservice.dto.VerifyOtpRequest;
import com.eeki.adminservice.service.AuthService;
import com.eeki.adminservice.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    public AuthController(AuthService authService, OtpService otpService) {
        this.authService = authService;
        this.otpService = otpService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        otpService.sendOtp(request);
        return ResponseEntity.ok("OTP sent to " + request.getPhoneNumber());
    }

    @PostMapping("/request-otp")
    public ResponseEntity<String> requestEmailOtp(@Valid @RequestBody EmailOtpRequest request) {
        otpService.sendOtpByEmail(request.email());
        return ResponseEntity.ok("OTP sent to " + request.email());
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyOtpAndAuthenticate(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is running");
    }
}
