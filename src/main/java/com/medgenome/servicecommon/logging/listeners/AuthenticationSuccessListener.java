package com.medgenome.servicecommon.logging.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "logging.security", name = "enabled", havingValue = "true")
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private static final Logger logger = LoggerFactory.getLogger("com.medgenome.security");

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        MDC.put("userId", username);
        logger.info("Authentication success for user '{}'", username);
        MDC.clear();
    }
}

