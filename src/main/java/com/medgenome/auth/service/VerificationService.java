package com.medgenome.auth.service;

import com.medgenome.auth.entity.User;
import com.medgenome.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class VerificationService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final OtpService otpService;

    public VerificationService(UserRepository userRepository, EmailService emailService,
                                OtpService otpService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.otpService = otpService;
    }

    public void initiateVerification(User user) {
        String emailToken = UUID.randomUUID().toString();
        String otp = String.format("%06d", new SecureRandom().nextInt(999999));

        user.setEmailVerificationToken(emailToken);
        user.setPhoneVerificationOtp(otp);
        user.setEmailTokenExpiry(Instant.now().plus(24, ChronoUnit.HOURS));
        user.setOtpExpiry(Instant.now().plus(10, ChronoUnit.MINUTES));

        userRepository.save(user);

        // Send email
        String verificationLink = "https://medgenome.com/api/verify-email?token=" + emailToken;
        emailService.send(user.getEmail(), "Verify your Email", "Click to verify: " + verificationLink);

        // Send OTP
        otpService.sendSmsOtp(user.getPhone(), user.getTenant().getTenantId());
    }
}

