package io.linc.auth.service;


import io.linc.auth.config.MultiTenantAuthProperties;
import io.linc.auth.dto.SsoLoginRequest;
import io.linc.auth.dto.SsoResponse;
import io.linc.auth.entity.Application;
import io.linc.auth.entity.RefreshToken;
import io.linc.auth.entity.SsoSession;
import io.linc.auth.entity.User;
import io.linc.auth.repository.RefreshTokenRepository;
import io.linc.auth.repository.SsoSessionRepository;
import io.linc.auth.repository.UserRepository;
import io.linc.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDate;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SsoService {
    private final UserRepository userRepository;
    private final SsoSessionRepository ssoSessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider tokenProvider;
    private final MultiTenantAuthProperties properties;
    private final PasswordEncoder passwordEncode;
    private final TenantMasterService tenantMasterService;

    @Transactional
    public SsoResponse login(SsoLoginRequest request) {
        User user = userRepository.findByEmail(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncode.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        Integer tenantId = tenantMasterService.getFirstTenantId()
                .orElseThrow(() -> new RuntimeException("Tenant not found in tenant_master"));

        // Invalidate existing SSO sessions
        ssoSessionRepository.findByUsernameAndTenantIdAndActiveTrue(user.getEmail(), tenantId)
                .ifPresent(session -> {
                    session.setActive(false);
                    ssoSessionRepository.save(session);
                });

        // Invalidate existing refresh tokens
        refreshTokenRepository.deleteByUsername(user.getEmail());

        // Create new SSO session
        SsoSession session = new SsoSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setUsername(user.getEmail());
        session.setTenantId(tenantId);
        session.setClientId(user.getTenant().getApplications().stream()
                .map(Application::getName)
                .collect(Collectors.joining(",")));
        session.setExpiryDate(LocalDateTime.ofInstant(Instant.now()
                        .plusSeconds(properties.getOtp().getValidityMinutes() * 60L), ZoneId.systemDefault())
                .toLocalDate());
        session.setLastAccessedAt(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).toLocalDate());

        session = ssoSessionRepository.save(session);

        // Token generation is now handled by the IAM service
        // This service only validates tokens
        throw new UnsupportedOperationException(
                "Token generation is no longer supported in this common service. " +
                "Please use the IAM service to generate tokens. " +
                "This service is for token validation only.");
    }

    @Transactional
    public SsoResponse refresh(String sessionId, String refreshToken) {
        SsoSession session = ssoSessionRepository.findBySessionIdAndActiveTrue(sessionId)
                .orElseThrow(() -> new RuntimeException("Invalid or expired SSO session"));

        RefreshToken existingRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (!existingRefreshToken.getUsername().equals(session.getUsername()) ||
                !existingRefreshToken.getTenantId().equals(session.getTenantId())) {
            throw new RuntimeException("Invalid refresh token for session");
        }

        if (existingRefreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(existingRefreshToken);
            throw new RuntimeException("Refresh token expired");
        }

        // Token generation is now handled by the IAM service
        throw new UnsupportedOperationException(
                "Token generation is no longer supported in this common service. " +
                "Please use the IAM service to generate tokens.");
    }

    @Transactional
    public void validateSession(String sessionId, Integer tenantId) {
        SsoSession session = ssoSessionRepository.findBySessionIdAndActiveTrue(sessionId)
                .orElseThrow(() -> new RuntimeException("Invalid or expired SSO session"));

        if (!session.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Invalid tenant for SSO session");
        }

        if (session.getExpiryDate().isBefore(ChronoLocalDate.from(Instant.now()))) {
            session.setActive(false);
            ssoSessionRepository.save(session);
            throw new RuntimeException("SSO session expired");
        }

        session.setLastAccessedAt(LocalDate.from(Instant.now()));
        ssoSessionRepository.save(session);
    }

    @Transactional
    public void logout(String sessionId) {
        SsoSession session = ssoSessionRepository.findBySessionIdAndActiveTrue(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        session.setActive(false);
        ssoSessionRepository.save(session);

        // Clean up refresh tokens
        refreshTokenRepository.deleteByUsername(session.getUsername());
    }

    @Transactional
    public SsoResponse loginWithTenantMaster() {
        // Token generation is now handled by the IAM service
        throw new UnsupportedOperationException(
                "Token generation is no longer supported in this common service. " +
                "Please use the IAM service to generate tokens. " +
                "This service is for token validation only.");
    }
}