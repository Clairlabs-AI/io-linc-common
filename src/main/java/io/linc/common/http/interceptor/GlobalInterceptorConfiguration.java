package io.linc.common.http.interceptor;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for HTTP request interceptors.
 * Registers all active interceptors with the Spring MVC framework.
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnWebApplication
@ConditionalOnClass(DispatcherServlet.class)
@EnableConfigurationProperties(InterceptorProperties.class)
public class GlobalInterceptorConfiguration implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(GlobalInterceptorConfiguration.class);

    private final InterceptorProperties properties;
    private final RequestMetricsInterceptor requestMetricsInterceptor;
    private final CorrelationIdInterceptor correlationIdInterceptor;
    private final SecurityTokenInterceptor securityTokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (properties.isMetricsEnabled()) {
            registry.addInterceptor(requestMetricsInterceptor)
                    .addPathPatterns(properties.getMetricsPathPatterns())
                    .excludePathPatterns(properties.getMetricsExcludedPathPatterns());
            log.info("Registered RequestMetricsInterceptor");
        }
        
        if (properties.isCorrelationIdEnabled()) {
            registry.addInterceptor(correlationIdInterceptor)
                    .addPathPatterns(properties.getCorrelationIdPathPatterns())
                    .excludePathPatterns(properties.getCorrelationIdExcludedPathPatterns());
            log.info("Registered CorrelationIdInterceptor");
        }
        
        if (properties.isSecurityTokenEnabled()) {
            registry.addInterceptor(securityTokenInterceptor)
                    .addPathPatterns(properties.getSecurityTokenPathPatterns())
                    .excludePathPatterns(properties.getSecurityTokenExcludedPathPatterns());
            log.info("Registered SecurityTokenInterceptor");
        }
    }
}