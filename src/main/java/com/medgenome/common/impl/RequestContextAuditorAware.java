package com.medgenome.common.impl;


import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class RequestContextAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        // Fetch user from security context or request context
        return RequestContextHolder.getUsername()
                .filter(username -> !username.isEmpty());
    }
}

