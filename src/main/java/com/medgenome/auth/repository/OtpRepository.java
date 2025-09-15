package com.medgenome.auth.repository;

import com.medgenome.auth.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface OtpRepository extends JpaRepository<UserOtp, Long> {
    Optional<UserOtp> findByCodeAndIdentifierAndTenantIdAndUsedFalse(
        String code, String identifier, Integer tenantId);

    @Transactional(readOnly = true)
    @Query("SELECT r.name FROM UserRole ur JOIN ur.role r JOIN r.tenant t WHERE ur.user.id = :userId AND t.tenantId = :tenantId")
    List<UserRole> findRolesByUserAndTenant(@Param("userId") Long userId, @Param("tenantId") Integer tenantId);

    @Transactional(readOnly = true)
    @Query("SELECT a.name FROM Application a JOIN a.tenant t WHERE t.tenantId = :tenantId")
    List<Application> findApplicationsByTenant(@Param("tenantId") Integer tenantId);

    @Transactional(readOnly = true)
    @Query("SELECT d.name FROM Domain d JOIN d.tenant t WHERE t.tenantId = :tenantId")
    List<Domain> findDomainsByTenant(@Param("tenantId") Integer tenantId);
}