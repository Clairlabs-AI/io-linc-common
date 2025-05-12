package com.medgenome.servicecommon.logging.config;

import com.medgenome.servicecommon.logging.aspect.PerformanceLoggingAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(PerformanceLoggingAspect.class)
@ConditionalOnProperty(name = "logging.performance.enabled", havingValue = "true", matchIfMissing = true)
public class PerformanceLoggingAutoConfiguration {

    @Bean
    public PerformanceLoggingAspect performanceLoggingAspect() {
        return new PerformanceLoggingAspect();
    }
}

