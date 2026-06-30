package com.eeki.adminservice.repository;

import com.eeki.adminservice.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findByPhoneNumberAndOtpCode(String phoneNumber, String otpCode);
    Optional<OtpVerification> findByPhoneNumber(String phoneNumber);
    Optional<OtpVerification> findByEmailAndOtpCode(String email, String otpCode);
    Optional<OtpVerification> findByEmail(String email);
    void deleteByPhoneNumberAndExpiresAtBefore(String phoneNumber, LocalDateTime expiresAt);
}
