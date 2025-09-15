package com.medgenome.auth.repository;

import com.medgenome.auth.entity.Role;
import com.medgenome.auth.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<UserRole> findByNameAndTenant_TenantId(String name, Integer tenantId);
}
