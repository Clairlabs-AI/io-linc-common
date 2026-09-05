package io.linc.auth.entity;


import io.linc.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "tenants")
public class Tenant extends AuditableEntity {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "tenant_id", length = 11, nullable = false, updatable = false, unique = true)
    private Integer tenantId;

    @Column(nullable = false)
    private String name;


    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL)
    private List<Application> applications;

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL)
    private List<User> users;

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL)
    private List<Role> roles;

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL)
    private List<Domain> domains;
}
