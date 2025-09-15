package com.medgenome.auth.service;


import com.medgenome.auth.config.MultiTenantAuthProperties;
import com.medgenome.auth.dto.SsoLoginRequest;
import com.medgenome.auth.dto.SsoResponse;
import com.medgenome.auth.entity.*;
import com.medgenome.auth.repository.RefreshTokenRepository;
import com.medgenome.auth.repository.SsoSessionRepository;
import com.medgenome.auth.repository.UserRepository;
import com.medgenome.auth.security.JwtTokenProvider;
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

        // Create new refresh token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(tokenProvider.generateToken(
                user.getEmail(),
                tenantId,
                user.getUserRoles().stream()
                        .map(userRole -> userRole.getRole().getName())
                        .toList(),
                user.getTenant().getApplications().stream()
                        .map(Application::getName)
                        .toList(),
                user.getTenant().getDomains().stream()
                        .map(Domain::getName)
                        .toList()
        ));

        log.info("token length: {}", refreshToken.getToken().length());
        refreshToken.setUsername(user.getEmail());
        refreshToken.setTenantId(tenantId);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(properties.getJwt().getRefreshTokenValidityHours() * 60 * 60));
        refreshToken = refreshTokenRepository.save(refreshToken);

        // Create access token
        String accessToken = tokenProvider.generateToken(
                user.getEmail(),
                tenantId,
                user.getUserRoles().stream()
                        .map(userRole -> userRole.getRole().getName())
                        .toList(),
                user.getTenant().getApplications().stream()
                        .map(Application::getName)
                        .toList(),
                user.getTenant().getDomains().stream()
                        .map(Domain::getName)
                        .toList()
        );

        return SsoResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .sessionId(session.getSessionId())
                .tokenType("Bearer")
                .expiresIn(properties.getJwt().getAccessTokenValidityMinutes() * 60)
                .build();
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

        User user = userRepository.findByEmail(session.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Integer tenantId = tenantMasterService.getFirstTenantId()
                .orElseThrow(() -> new RuntimeException("Tenant not found in tenant_master"));

        // Create new access token
        String accessToken = tokenProvider.generateToken(
                user.getEmail(),
                tenantId,
                user.getUserRoles().stream()
                        .map(userRole -> userRole.getRole().getName())
                        .toList(),
                user.getTenant().getApplications().stream()
                        .map(Application::getName)
                        .toList(),
                user.getTenant().getDomains().stream()
                        .map(Domain::getName)
                        .toList()
        );

        // Update session last accessed time
        session.setLastAccessedAt(LocalDate.from(Instant.now()));
        ssoSessionRepository.save(session);

        return SsoResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .sessionId(session.getSessionId())
                .tokenType("Bearer")
                .expiresIn(properties.getJwt().getRefreshTokenValidityHours() * 60 * 60) // in seconds
                .build();
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
        // Always use static username and password for token creation
        String username = "Medgenome@gmail.com";
        Integer tenantId = 1;
        String accessToken = tokenProvider.generateToken(
            username,
            tenantId,
            null, // roles
            null, // apps
            null, // domains
            30 * 60 * 1000 // 30 min in ms
        );
        String refreshTokenStr = tokenProvider.generateToken(
            username,
            tenantId,
            null,
            null,
            null,
            2 * 60 * 60 * 1000 // 2 hours in ms
        );
        return SsoResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshTokenStr)
            .sessionId("")
            .tokenType("Bearer")
            .expiresIn(30 * 60) // 30 min in seconds
            .build();
    }
}