package com.medgenome.auth.service;

import com.medgenome.auth.dto.AuthRequest;
import com.medgenome.auth.dto.TokenDetails;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String tenantId = extractTenantId(authentication);
            if (tenantId != null) {
                session.enableFilter("tenantFilter")
                        .setParameter("tenantId", tenantId);
            }
        }
    }

    /**
     * Extracts tenant ID from authentication details.
     * Supports both TokenDetails (new) and AuthRequest (legacy) for backward compatibility.
     *
     * @param authentication Spring Security authentication object
     * @return tenant ID as String, or null if not found
     */
    private String extractTenantId(Authentication authentication) {
        Object details = authentication.getDetails();
        
        if (details instanceof TokenDetails tokenDetails) {
            return String.valueOf(tokenDetails.tenantId());
        } else if (details instanceof AuthRequest authRequest) {
            return String.valueOf(authRequest.getTenantId());
        } else if (details instanceof Integer) {
            // Backward compatibility: if details is directly an Integer (tenantId)
            return String.valueOf(details);
        } else if (details instanceof String) {
            // Backward compatibility: if details is directly a String (tenantId)
            return (String) details;
        }
        
        return null;
    }
}