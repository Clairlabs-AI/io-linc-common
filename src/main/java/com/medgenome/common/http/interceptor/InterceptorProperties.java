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

    public boolean isMetricsEnabled() {
        return metricsEnabled;
    }

    public void setMetricsEnabled(boolean metricsEnabled) {
        this.metricsEnabled = metricsEnabled;
    }

    public List<String> getMetricsPathPatterns() {
        return metricsPathPatterns;
    }

    public void setMetricsPathPatterns(List<String> metricsPathPatterns) {
        this.metricsPathPatterns = metricsPathPatterns;
    }

    public List<String> getMetricsExcludedPathPatterns() {
        return metricsExcludedPathPatterns;
    }

    public void setMetricsExcludedPathPatterns(List<String> metricsExcludedPathPatterns) {
        this.metricsExcludedPathPatterns = metricsExcludedPathPatterns;
    }

    public boolean isCorrelationIdEnabled() {
        return correlationIdEnabled;
    }

    public void setCorrelationIdEnabled(boolean correlationIdEnabled) {
        this.correlationIdEnabled = correlationIdEnabled;
    }

    public List<String> getCorrelationIdPathPatterns() {
        return correlationIdPathPatterns;
    }

    public void setCorrelationIdPathPatterns(List<String> correlationIdPathPatterns) {
        this.correlationIdPathPatterns = correlationIdPathPatterns;
    }

    public List<String> getCorrelationIdExcludedPathPatterns() {
        return correlationIdExcludedPathPatterns;
    }

    public void setCorrelationIdExcludedPathPatterns(List<String> correlationIdExcludedPathPatterns) {
        this.correlationIdExcludedPathPatterns = correlationIdExcludedPathPatterns;
    }

    public String getCorrelationIdHeaderName() {
        return correlationIdHeaderName;
    }

    public void setCorrelationIdHeaderName(String correlationIdHeaderName) {
        this.correlationIdHeaderName = correlationIdHeaderName;
    }

    public boolean isGenerateCorrelationIdIfMissing() {
        return generateCorrelationIdIfMissing;
    }

    public void setGenerateCorrelationIdIfMissing(boolean generateCorrelationIdIfMissing) {
        this.generateCorrelationIdIfMissing = generateCorrelationIdIfMissing;
    }

    public boolean isSecurityTokenEnabled() {
        return securityTokenEnabled;
    }

    public void setSecurityTokenEnabled(boolean securityTokenEnabled) {
        this.securityTokenEnabled = securityTokenEnabled;
    }

    public List<String> getSecurityTokenPathPatterns() {
        return securityTokenPathPatterns;
    }

    public void setSecurityTokenPathPatterns(List<String> securityTokenPathPatterns) {
        this.securityTokenPathPatterns = securityTokenPathPatterns;
    }

    public List<String> getSecurityTokenExcludedPathPatterns() {
        return securityTokenExcludedPathPatterns;
    }

    public void setSecurityTokenExcludedPathPatterns(List<String> securityTokenExcludedPathPatterns) {
        this.securityTokenExcludedPathPatterns = securityTokenExcludedPathPatterns;
    }

    public String getSecurityTokenHeaderName() {
        return securityTokenHeaderName;
    }

    public void setSecurityTokenHeaderName(String securityTokenHeaderName) {
        this.securityTokenHeaderName = securityTokenHeaderName;
    }

    public String getSecurityTokenPrefix() {
        return securityTokenPrefix;
    }

    public void setSecurityTokenPrefix(String securityTokenPrefix) {
        this.securityTokenPrefix = securityTokenPrefix;
    }
}