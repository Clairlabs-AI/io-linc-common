package com.medgenome.servicecommon.logging.config;

import com.medgenome.servicecommon.logging.impl.RequestContextAuditorAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingAutoConfiguration {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return new RequestContextAuditorAware();
    }
}

