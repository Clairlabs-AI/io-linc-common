package io.linc.common;

import io.linc.auth.config.MultiTenantAuthProperties;
import io.linc.auth.service.TenantFilterService;
import io.linc.common.aop.AuditLoggingAspect;
import io.linc.common.aop.ExceptionLoggingAspect;
import io.linc.common.aop.PerformanceLoggingAspect;
import io.linc.common.config.JpaAuditingAutoConfiguration;
import io.linc.common.config.PriorityConfigurationProperties;
import io.linc.common.config.PriorityPropertySource;
import io.linc.common.db.DatabaseConfiguration;
import io.linc.common.db.DatabaseProperties;
import io.linc.common.http.exception.GlobalExceptionHandler;
import io.linc.common.http.interceptor.*;
import io.linc.common.logging.LoggingConfiguration;
import io.linc.common.logging.LoggingProperties;
import io.linc.common.logging.RequestLoggingConfiguration;
import io.linc.common.logging.RequestLoggingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
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
        "io.linc.auth.repository"
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