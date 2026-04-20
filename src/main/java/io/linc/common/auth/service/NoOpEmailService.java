package io.linc.common.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "auth.mail", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoOpEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(NoOpEmailService.class);

    @Override
    public void send(String to, String subject, String content) {
        log.info("NoOpEmailService: Mail sending disabled. To='{}', Subject='{}'", to, subject);
    }
}

