package com.medgenome.common;

import com.medgenome.auth.config.MultiTenantAuthProperties;
import com.medgenome.auth.service.TenantFilterService;
import com.medgenome.common.aop.AuditLoggingAspect;
import com.medgenome.common.aop.ExceptionLoggingAspect;
import com.medgenome.common.aop.PerformanceLoggingAspect;
import com.medgenome.common.config.JpaAuditingAutoConfiguration;
import com.medgenome.common.config.PriorityConfigurationProperties;
import com.medgenome.common.config.PriorityPropertySource;
import com.medgenome.common.db.DatabaseConfiguration;
import com.medgenome.common.db.DatabaseProperties;
import com.medgenome.common.http.exception.GlobalExceptionHandler;
import com.medgenome.common.http.interceptor.*;
import com.medgenome.common.logging.LoggingConfiguration;
import com.medgenome.common.logging.LoggingProperties;
import com.medgenome.common.logging.RequestLoggingConfiguration;
import com.medgenome.common.logging.RequestLoggingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Auto-configuration class for the common library.
 * Imports all configuration classes and sets up property sources.
 */
@Configuration
@EnableConfigurationProperties({
    LoggingProperties.class,
    RequestLoggingProperties.class,
    InterceptorProperties.class,
    DatabaseProperties.class,
    PriorityConfigurationProperties.class,
    MultiTenantAuthProperties.class
})
@Import({
    // Logging
    LoggingConfiguration.class,
    RequestLoggingConfiguration.class,
    JpaAuditingAutoConfiguration.class,
    
    // AOP
    PerformanceLoggingAspect.class,
    AuditLoggingAspect.class,
    ExceptionLoggingAspect.class,
    
    // HTTP
    GlobalInterceptorConfiguration.class,
    CorrelationIdInterceptor.class,
    RequestMetricsInterceptor.class,
    SecurityTokenInterceptor.class,
    GlobalExceptionHandler.class,
    
    // Database
    DatabaseConfiguration.class,

    // Tenant
    TenantFilterService.class

})
@EnableJpaRepositories(basePackages = {
        "com.medgenome.auth.repository"
})
@ConditionalOnProperty(name = "common.enabled", havingValue = "true", matchIfMissing = true)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class CommonLibraryAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CommonLibraryAutoConfiguration.class);

    @Bean
    public PriorityPropertySource priorityPropertySource(
            PriorityConfigurationProperties properties, 
            ConfigurableEnvironment environment) {
        
        PriorityPropertySource propertySource = new PriorityPropertySource(
                "priorityProperties", properties);
        
        environment.getPropertySources().addFirst(propertySource);
        log.info("Registered priority property source with environment");
        
        return propertySource;
    }
}