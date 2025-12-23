package com.medgenome.auth.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.sql.Timestamp;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "tenant_master")
public class TenantMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tenant_id", nullable = false, updatable = false, unique = true)
    private Integer tenantId;

    @Column(name = "tenant_name", length = 50, nullable = false)
    private String tenantName;

    @Column(name = "tenant_status", columnDefinition = "INT(2) DEFAULT 1")
    private Integer tenantStatus = 1;

    @Column(name = "tenant_code", length = 36, nullable = false, unique = true)
    private String tenantCode;

    @Column(name = "tenant_address", columnDefinition = "json")
    private String tenantAddress;

    @Column(name = "website_address", length = 100)
    private String websiteAddress;

    @Column(name = "phone_number", columnDefinition = "json", nullable = false)
    private String phoneNumber;

    @Column(name = "primary_contact_name", length = 40)
    private String primaryContactName;

    @Column(name = "secondary_contact_name", length = 40)
    private String secondaryContactName;

    @Column(name = "email_id", length = 100, nullable = false, unique = true)
    private String emailId;

    @Column(name = "plan_type", length = 20)
    private String planType;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP")
    private Timestamp updatedAt;

    @Column(name = "created_by", length = 50, nullable = false)
    private String createdBy;

    @Column(name = "updated_by", length = 50, nullable = false)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }
}
