package com.medgenome.auth.service;

import com.medgenome.auth.dto.AuthRequest;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.hibernate.Session;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class TenantFilterService {

    private final EntityManager entityManager;

    public TenantFilterService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public void applyTenantFilter() {
        Session session = entityManager.unwrap(Session.class);
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            String tenantId = ((AuthRequest) SecurityContextHolder.getContext().getAuthentication().getDetails()).getTenantId();
            if (tenantId != null) {
                session.enableFilter("tenantFilter")
                        .setParameter("tenantId", tenantId);
            }
        }
    }
}