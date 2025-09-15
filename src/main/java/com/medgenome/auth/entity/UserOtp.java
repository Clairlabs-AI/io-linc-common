package com.medgenome.auth.entity;

import com.medgenome.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "user_otps")
public class UserOtp extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String identifier; // email or phone

    @Column(nullable = false)
    private Integer tenantId;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private boolean used = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OtpType type;

    public enum OtpType {
        EMAIL, SMS
    }

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}