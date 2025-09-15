package com.medgenome.auth.entity;


import com.medgenome.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "sso_sessions")
public class SsoSession extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sessionId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Integer tenantId;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private LocalDate lastAccessedAt;

    @Column(nullable = false)
    private String clientId;
}