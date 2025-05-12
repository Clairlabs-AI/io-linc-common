package com.medgenome.servicecommon.logging.config;

import com.medgenome.servicecommon.logging.aspect.SecurityLoggingAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(SecurityLoggingAspect.class)
@ConditionalOnProperty(name = "logging.security.enabled", havingValue = "true", matchIfMissing = true)
public class SecurityLoggingAutoConfiguration {

    @Bean
    public SecurityLoggingAspect securityLoggingAspect() {
        return new SecurityLoggingAspect();
    }
}

