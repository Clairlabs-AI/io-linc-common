package io.linc.common.auth.repository;

import io.linc.common.auth.entity.Role;
import io.linc.common.auth.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<UserRole> findByNameAndTenant_TenantId(String name, Integer tenantId);
}
