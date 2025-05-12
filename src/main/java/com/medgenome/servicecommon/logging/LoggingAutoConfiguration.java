package com.medgenome.servicecommon.logging;


import com.medgenome.servicecommon.logging.aspect.AuditLoggingAspect;
import com.medgenome.servicecommon.logging.aspect.PerformanceLoggingAspect;
import com.medgenome.servicecommon.logging.aspect.SecurityLoggingAspect;
import com.medgenome.servicecommon.logging.properties.LoggingProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LoggingProperties.class)
public class LoggingAutoConfiguration {

    @Bean
    public AuditLoggingAspect auditLoggingAspect() {
        return new AuditLoggingAspect();
    }

    @Bean
    public PerformanceLoggingAspect performanceLoggingAspect() {
        return new PerformanceLoggingAspect();
    }

    @Bean
    public SecurityLoggingAspect securityLoggingAspect() {
        return new SecurityLoggingAspect();
    }
}
