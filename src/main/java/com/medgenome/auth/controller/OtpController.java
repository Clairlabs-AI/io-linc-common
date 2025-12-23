/*
package com.medgenome.auth.controller;

import com.medgenome.auth.config.MultiTenantAuthProperties;
import com.medgenome.auth.dto.OtpRequest;
import com.medgenome.auth.dto.OtpVerificationRequest;
import com.medgenome.auth.dto.SsoResponse;
import com.medgenome.auth.entity.Application;
import com.medgenome.auth.entity.Domain;
import com.medgenome.auth.entity.Role;
import com.medgenome.auth.entity.UserRole;
import com.medgenome.auth.security.JwtTokenProvider;
import com.medgenome.auth.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth/otp")
@RequiredArgsConstructor
public class OtpController {

    private final OtpService otpService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MultiTenantAuthProperties properties;

    @PostMapping("/email/send")
    public ResponseEntity<Void> sendEmailOtp(@Valid @RequestBody OtpRequest request) {
        otpService.sendEmailOtp(request.getIdentifier(), request.getTenantId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sms/send")
    public ResponseEntity<Void> sendSmsOtp(@Valid @RequestBody OtpRequest request) {
        otpService.sendSmsOtp(request.getIdentifier(), request.getTenantId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify")
    public ResponseEntity<SsoResponse> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        boolean isValid = otpService.verifyOtp(
                request.getCode(),
                request.getIdentifier(),
                request.getTenantId()
        );

        if (isValid) {
            Long userId = Long.parseLong(request.getIdentifier()); // Assuming identifier is userId
            Integer tenantId = request.getTenantId();

            List<String> roles = otpService.getRolesByUserAndTenant(userId, tenantId);
            List<String> applications = otpService.getApplicationsByTenant(tenantId);
            List<String> domains = otpService.getDomainsByTenant(tenantId);

            String accessToken = jwtTokenProvider.generateToken(request.getIdentifier(),
                    request.getTenantId(), roles, applications,domains);


            SsoResponse response = SsoResponse.builder()
                    .accessToken(accessToken)
                    .tokenType("Bearer")
                    .expiresIn(properties.getJwt().getAccessTokenValidityMinutes())
                    .build();
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(401).build(); // Unauthorized if OTP verification fails
    }
}*/
