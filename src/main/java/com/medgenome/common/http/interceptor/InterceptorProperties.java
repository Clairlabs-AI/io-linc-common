package com.medgenome.common.http.interceptor;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for HTTP request interceptors.
 */
@Data
@ConfigurationProperties(prefix = "common.interceptor")
public class InterceptorProperties {

    /**
     * Request metrics interceptor configuration.
     */
    private boolean metricsEnabled = true;
    private List<String> metricsPathPatterns = List.of("/**");
    private List<String> metricsExcludedPathPatterns = List.of("/health/**", "/actuator/**");


    /**
     * Correlation ID interceptor configuration.
     */
    private boolean correlationIdEnabled = true;
    private List<String> correlationIdPathPatterns = List.of("/**");
    private List<String> correlationIdExcludedPathPatterns = new ArrayList<>();
    private String correlationIdHeaderName = "X-Correlation-ID";
    private boolean generateCorrelationIdIfMissing = true;

    /**
     * Security token interceptor configuration.
     */
    private boolean securityTokenEnabled = false;
    private List<String> securityTokenPathPatterns = List.of("/**");
    private List<String> securityTokenExcludedPathPatterns = List.of("/public/**", "/api/auth/**");
    private String securityTokenHeaderName = "Authorization";
    private String securityTokenPrefix = "Bearer ";
}