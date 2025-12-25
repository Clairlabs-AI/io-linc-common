package com.medgenome.common.http.interceptor;

import com.medgenome.auth.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor for validating security tokens in HTTP requests.
 * Basic implementation that can be customized for specific authentication schemes.
 */
@ConditionalOnWebApplication
@EnableConfigurationProperties(InterceptorProperties.class)
@ConditionalOnProperty(prefix = "common.interceptor", name = "security-token-enabled", havingValue = "true")
public class SecurityTokenInterceptor implements HandlerInterceptor, Ordered {

    private static final Logger log = LoggerFactory.getLogger(SecurityTokenInterceptor.class);

    private final InterceptorProperties properties;
    private final JwtTokenProvider tokenProvider;



    public SecurityTokenInterceptor(InterceptorProperties properties, JwtTokenProvider tokenProvider) {
        this.properties = properties;
        this.tokenProvider = tokenProvider;
        log.info("SecurityTokenInterceptor initialized with header: {}", properties.getSecurityTokenHeaderName());
    }


    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String requestPath = request.getRequestURI();

        // Check if the request path is in the excluded paths
        if (properties.getSecurityTokenExcludedPathPatterns() != null && properties.getSecurityTokenExcludedPathPatterns().contains(requestPath)) {
            log.info("Request path {} is excluded from security token validation.", requestPath);
            return true;
        }

        String authHeader = request.getHeader(properties.getSecurityTokenHeaderName());

        if (StringUtils.isBlank(authHeader) || !authHeader.startsWith(properties.getSecurityTokenPrefix())) {
            log.warn("Missing or invalid authorization header for request to {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        String token = authHeader.substring(properties.getSecurityTokenPrefix().length());

        // This is a template - should implement token validation logic here
        boolean isValid = tokenProvider.validateToken(token);

        if (!isValid) {
            log.warn("Invalid token for request to {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        return true;
    }



    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }
}