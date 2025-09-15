package com.medgenome.auth.entity;

import com.medgenome.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Integer tenantId;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column
    @ElementCollection
    private List<String> roles = new ArrayList<>();

    @Column
    @ElementCollection
    private List<String> apps = new ArrayList<>();

   /* @Bean
    public RefreshToken initializeRefreshToken(String username, Integer tenantId, int refreshTokenValidityHours) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(
                Instant.now().plusSeconds((long) refreshTokenValidityHours * 60 * 60)
        );
        refreshToken.setUsername(username);
        refreshToken.setTenantId(tenantId);
        return refreshToken;
    }*/
}