package com.medgenome.servicecommon.logging.config;

import com.medgenome.servicecommon.logging.aspect.AuditLoggingAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(AuditLoggingAspect.class)
@ConditionalOnProperty(name = "logging.audit.enabled", havingValue = "true", matchIfMissing = true)
public class AuditLoggingAutoConfiguration {

    @Bean
    public AuditLoggingAspect auditLoggingAspect() {
        return new AuditLoggingAspect();
    }
}

