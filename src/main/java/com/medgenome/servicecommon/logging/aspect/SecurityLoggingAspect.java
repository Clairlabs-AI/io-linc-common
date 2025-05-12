package com.medgenome.servicecommon.logging.aspect;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@ConditionalOnProperty(prefix = "logging.security", name = "enabled", havingValue = "true")
public class SecurityLoggingAspect {

    private final Logger logger = LoggerFactory.getLogger("com.medgenome.security");

    @AfterReturning("execution(* org.springframework.security.authentication.AuthenticationManager.authenticate(..))")
    public void logAuthenticationSuccess() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            MDC.put("userId", auth.getName());
            logger.info("Authentication success for user '{}'", auth.getName());
        }
        MDC.clear();
    }

    @AfterThrowing(pointcut = "execution(* org.springframework.security.authentication.AuthenticationManager.authenticate(..))",
            throwing = "ex")
    public void logAuthenticationFailure(Exception ex) {
        logger.warn("Authentication failure: {}", ex.getMessage());
    }
}

