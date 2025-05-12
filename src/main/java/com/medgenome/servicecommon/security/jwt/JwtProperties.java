package com.medgenome.servicecommon.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for JWT.
 */
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {
    /**
     * Secret key used for signing JWTs.
     */
    private String secretKey = "YjE4NzQ2MzQ0MmFkNzFhZTY0ZDNiZDZkMGQwMTg5MGRmMjM5YjY3OTkwMGZjMzk5ZGVkNmYwMmYzYzgwZGNiN2Y=";
    
    /**
     * Token expiration time in milliseconds.
     */
    private long expirationMs = 86400000; // 24 hours
    
    /**
     * Name of the header containing the JWT token.
     */
    private String headerName = "Authorization";
    
    /**
     * Prefix for the JWT token in the header.
     */
    private String tokenPrefix = "Bearer ";
    
    /**
     * URL paths that should be excluded from JWT authentication.
     */
    private String[] excludedPaths = {"/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/actuator/**"};

    /**
     * Gets the secret key.
     *
     * @return the secret key
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * Sets the secret key.
     *
     * @param secretKey the secret key to set
     */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * Gets the token expiration time in milliseconds.
     *
     * @return the token expiration time
     */
    public long getExpirationMs() {
        return expirationMs;
    }

    /**
     * Sets the token expiration time in milliseconds.
     *
     * @param expirationMs the token expiration time to set
     */
    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }

    /**
     * Gets the header name.
     *
     * @return the header name
     */
    public String getHeaderName() {
        return headerName;
    }

    /**
     * Sets the header name.
     *
     * @param headerName the header name to set
     */
    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    /**
     * Gets the token prefix.
     *
     * @return the token prefix
     */
    public String getTokenPrefix() {
        return tokenPrefix;
    }

    /**
     * Sets the token prefix.
     *
     * @param tokenPrefix the token prefix to set
     */
    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }

    /**
     * Gets the excluded paths.
     *
     * @return the excluded paths
     */
    public String[] getExcludedPaths() {
        return excludedPaths;
    }

    /**
     * Sets the excluded paths.
     *
     * @param excludedPaths the excluded paths to set
     */
    public void setExcludedPaths(String[] excludedPaths) {
        this.excludedPaths = excludedPaths;
    }
}