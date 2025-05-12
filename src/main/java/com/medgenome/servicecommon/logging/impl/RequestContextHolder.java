package com.medgenome.servicecommon.logging.impl;


import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class RequestContextHolder {

    private RequestContextHolder() {
        // Private constructor to prevent instantiation
    }

    public static String getUsername() {
        // Try to fetch from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName(); // or extract principal if needed
        }

        // Fallback to MDC if SecurityContext is unavailable
        return MDC.get("userId"); // Assumes userId is stored in MDC
    }
}

