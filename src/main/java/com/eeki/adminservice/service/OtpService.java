package com.eeki.adminservice.service;

import com.eeki.adminservice.dto.SendOtpRequest;
import com.eeki.adminservice.dto.VerifyOtpRequest;
import com.eeki.adminservice.entity.OtpVerification;
import com.eeki.adminservice.repository.OtpVerificationRepository;
import com.eeki.adminservice.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    private final OtpVerificationRepository otpRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final int OTP_LENGTH = 6;
    private static final String OTP_PREFIX = "otp:";
    private static final long OTP_VALIDITY_MINUTES = 10;

    public OtpService(OtpVerificationRepository otpRepository,
                      UserRepository userRepository,
                      RedisTemplate<String, Object> redisTemplate) {
        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public void sendOtp(SendOtpRequest request) {
        String otpCode = generateOtp();

        // Save to database
        OtpVerification otp = OtpVerification.builder()
                .phoneNumber(request.getPhoneNumber())
                .otpCode(otpCode)
                .verified(false)
                .attempts(0)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES))
                .build();

        otpRepository.save(otp);

        // Try to store in Redis for faster access (optional, fallback to DB if Redis unavailable)
        try {
            String redisKey = OTP_PREFIX + request.getPhoneNumber();
            redisTemplate.opsForValue().set(redisKey, otpCode, OTP_VALIDITY_MINUTES, TimeUnit.MINUTES);
        } catch (Exception ex) {
            // Redis not available, but that's OK - we'll use database only
            System.out.println("Redis not available, using database for OTP caching: " + ex.getMessage());
        }

        // In production, send SMS via Twilio or other provider
        System.out.println("OTP for " + request.getPhoneNumber() + ": " + otpCode);
    }

    @Transactional
    public void sendOtpByEmail(String email) {
        // Find user by email (case-insensitive)
        var userOptional = userRepository.findByEmailIgnoreCase(email);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found with email: " + email);
        }

        var user = userOptional.get();
        String phoneNumber = user.getPhoneNumber();
        
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new RuntimeException("User doesn't have a registered phone number");
        }

        String otpCode = generateOtp();

        // Save to database with both email and phone number
        OtpVerification otp = OtpVerification.builder()
                .phoneNumber(phoneNumber)
                .email(email)
                .otpCode(otpCode)
                .verified(false)
                .attempts(0)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES))
                .build();

        otpRepository.save(otp);

        // Try to store in Redis with email as key for faster lookup
        try {
            String redisKey = OTP_PREFIX + "email:" + email;
            redisTemplate.opsForValue().set(redisKey, otpCode, OTP_VALIDITY_MINUTES, TimeUnit.MINUTES);
        } catch (Exception ex) {
            System.out.println("Redis not available: " + ex.getMessage());
        }

        // In production, send email via SMTP
        System.out.println("OTP for email " + email + " (phone: " + phoneNumber + "): " + otpCode);
    }

    @Transactional
    public boolean verifyOtp(VerifyOtpRequest request) {
        // Priority: phone_number > email > (nothing)
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            return verifyPhoneOtp(request.getPhoneNumber(), request.getOtp());
        } else if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            return verifyEmailOtp(request.getEmail(), request.getOtp());
        }
        return false;
    }

    @Transactional
    private boolean verifyPhoneOtp(String phoneNumber, String otp) {
        try {
            // Try Redis first
            String redisKey = OTP_PREFIX + phoneNumber;
            Object cachedOtp = redisTemplate.opsForValue().get(redisKey);

            if (cachedOtp != null && cachedOtp.toString().equals(otp)) {
                // Mark as verified in database
                var otpRecord = otpRepository.findByPhoneNumber(phoneNumber);
                if (otpRecord.isPresent()) {
                    OtpVerification otpVerification = otpRecord.get();
                    otpVerification.setVerified(true);
                    otpRepository.save(otpVerification);
                }

                // Remove from Redis
                try {
                    redisTemplate.delete(redisKey);
                } catch (Exception ignored) {
                    // Redis delete failed, but verification already done
                }
                return true;
            }
        } catch (Exception ex) {
            // Redis not available, fall through to database check
            System.out.println("Redis verification failed for phone, checking database: " + ex.getMessage());
        }

        // Check database if not in Redis
        var otpRecord = otpRepository.findByPhoneNumberAndOtpCode(phoneNumber, otp);
        if (otpRecord.isPresent()) {
            OtpVerification otpVerification = otpRecord.get();
            if (otpVerification.getExpiresAt().isAfter(LocalDateTime.now())) {
                otpVerification.setVerified(true);
                otpRepository.save(otpVerification);
                return true;
            }
        }

        return false;
    }

    @Transactional
    private boolean verifyEmailOtp(String email, String otp) {
        // First verify user exists
        var userOptional = userRepository.findByEmailIgnoreCase(email);
        if (userOptional.isEmpty()) {
            return false;
        }

        try {
            // Try Redis first with email key
            String redisKey = OTP_PREFIX + "email:" + email;
            Object cachedOtp = redisTemplate.opsForValue().get(redisKey);

            if (cachedOtp != null && cachedOtp.toString().equals(otp)) {
                // Mark as verified in database
                var otpRecord = otpRepository.findByEmail(email);
                if (otpRecord.isPresent()) {
                    OtpVerification otpVerification = otpRecord.get();
                    otpVerification.setVerified(true);
                    otpRepository.save(otpVerification);
                }

                // Remove from Redis
                try {
                    redisTemplate.delete(redisKey);
                } catch (Exception ignored) {
                    // Redis delete failed, but verification already done
                }
                return true;
            }
        } catch (Exception ex) {
            // Redis not available, fall through to database check
            System.out.println("Redis verification failed for email, checking database: " + ex.getMessage());
        }

        // Check database if not in Redis
        var otpRecord = otpRepository.findByEmailAndOtpCode(email, otp);
        if (otpRecord.isPresent()) {
            OtpVerification otpVerification = otpRecord.get();
            if (otpVerification.getExpiresAt().isAfter(LocalDateTime.now())) {
                otpVerification.setVerified(true);
                otpRepository.save(otpVerification);
                return true;
            }
        }

        return false;
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
