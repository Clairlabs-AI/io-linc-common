package io.linc.auth.repository;

import io.linc.auth.entity.TenantMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TenantMasterRepository extends JpaRepository<TenantMaster, Integer> {
    Optional<TenantMaster> findByTenantCode(String tenantCode);
    Optional<TenantMaster> findByEmailId(String emailId);
    TenantMaster findByTenantId(Integer tenantId);
}

