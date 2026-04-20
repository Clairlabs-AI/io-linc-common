package io.linc.common.auth.repository;


import io.linc.common.auth.entity.SsoSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SsoSessionRepository extends JpaRepository<SsoSession, Long> {
    Optional<SsoSession> findBySessionIdAndActiveTrue(String sessionId);
    Optional<SsoSession> findByUsernameAndTenantIdAndActiveTrue(String username, Integer tenantId);
    void deleteByUsername(String username);
}
