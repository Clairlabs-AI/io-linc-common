package com.medgenome.servicecommon.logging.impl;


import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class RequestContextAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        // Fetch user from security context or request context
        return Optional.ofNullable(RequestContextHolder.getUsername())
                .filter(username -> !username.isBlank());
    }
}

