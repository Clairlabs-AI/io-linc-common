package com.medgenome.servicecommon.logging.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "logging.security", name = "enabled", havingValue = "true")
public class AuthenticationFailureListener implements ApplicationListener<AbstractAuthenticationFailureEvent> {

    private static final Logger logger = LoggerFactory.getLogger("com.medgenome.security");

    @Override
    public void onApplicationEvent(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();
        logger.warn("Authentication failed for user '{}': {}", username, event.getException().getMessage());
    }
}
