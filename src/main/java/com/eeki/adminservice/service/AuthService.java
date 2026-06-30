package com.eeki.adminservice.service;

import com.eeki.adminservice.config.JwtTokenProvider;
import com.eeki.adminservice.dto.AuthResponse;
import com.eeki.adminservice.dto.LoginRequest;
import com.eeki.adminservice.dto.RegisterRequest;
import com.eeki.adminservice.dto.UserDTO;
import com.eeki.adminservice.dto.VerifyOtpRequest;
import com.eeki.adminservice.entity.User;
import com.eeki.adminservice.entity.UserRole;
import com.eeki.adminservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final OtpService otpService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       OtpService otpService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.otpService = otpService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Phone number already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.ROLE_USER)
                .active(true)
                .phoneVerified(false)
                .emailVerified(false)
                .build();

        user = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().toString());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        UserDTO userDTO = mapToDTO(user);

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(userDTO)
                .message("User registered successfully")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (!user.getActive()) {
            throw new RuntimeException("User account is inactive");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().toString());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        UserDTO userDTO = mapToDTO(user);

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(userDTO)
                .message("Login successful")
                .build();
    }

    @Transactional
    public AuthResponse verifyOtpAndAuthenticate(VerifyOtpRequest request) {
        // Verify the OTP
        boolean verified = otpService.verifyOtp(request);
        if (!verified) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        // Find user - either by email or phone number
        User user = null;
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            user = userRepository.findByEmailIgnoreCase(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found with this email"));
        } else if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                    .orElseThrow(() -> new RuntimeException("User not found with this phone number"));
        } else {
            throw new RuntimeException("Email or phone number is required");
        }

        if (!user.getActive()) {
            throw new RuntimeException("User account is inactive");
        }

        // Mark phone as verified
        user.setPhoneVerified(true);
        userRepository.save(user);

        // Generate JWT tokens
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().toString());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        // Map user to DTO
        UserDTO userDTO = mapToDTO(user);

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(userDTO)
                .message("OTP verified successfully")
                .build();
    }

    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .phoneVerified(user.getPhoneVerified())
                .emailVerified(user.getEmailVerified())
                .active(user.getActive())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
