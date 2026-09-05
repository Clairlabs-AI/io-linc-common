package io.linc.auth.controller;

import io.linc.auth.dto.PhoneOtpRequest;
import io.linc.auth.entity.User;
import io.linc.auth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class VerificationController {

    private final UserRepository userRepository;

    public VerificationController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        Optional<User> userOpt = userRepository.findByEmailVerificationToken(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getEmailTokenExpiry().isAfter(Instant.now())) {
                user.setEmailVerified(true);
                user.setEmailVerificationToken(null);
                userRepository.save(user);
                return ResponseEntity.ok("Email verified successfully.");
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token.");
    }

    @PostMapping("/verify-phone")
    public ResponseEntity<String> verifyPhone(@RequestBody PhoneOtpRequest request) {
        Optional<User> userOpt = userRepository.findByPhone(request.getPhone());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPhoneVerificationOtp().equals(request.getOtp()) &&
                    user.getOtpExpiry().isAfter(Instant.now())) {
                user.setPhoneVerified(true);
                user.setPhoneVerificationOtp(null);
                userRepository.save(user);
                return ResponseEntity.ok("Phone verified successfully.");
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP or phone number.");
    }

}

