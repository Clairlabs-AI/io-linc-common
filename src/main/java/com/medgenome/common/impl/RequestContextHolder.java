package com.medgenome.common.impl;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class RequestContextHolder {

    private RequestContextHolder() {
        // Private constructor to prevent instantiation
    }

    public static Optional<String> getUsername() {
        // Try to fetch from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Optional.ofNullable(authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())
                ? authentication.getName()
                : "system");
    }
}

