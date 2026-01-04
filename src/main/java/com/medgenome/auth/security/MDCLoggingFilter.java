package com.medgenome.auth.security;

import com.medgenome.auth.dto.TokenDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to populate MDC (Mapped Diagnostic Context) with request and user information for logging.
 * This filter runs early to set up basic request context, and user details are added by JwtAuthenticationFilter.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MDCLoggingFilter extends OncePerRequestFilter {

    @Value("${app.name:unknown}")
    private String appName;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws IOException, ServletException {

        // Set basic request context
        MDC.put("correlationId", request.getHeader("X-CorrelationId"));
        MDC.put("messageId", request.getHeader("X-MessageId"));
        MDC.put("clientIp", request.getRemoteAddr());
        MDC.put("requestPath", request.getRequestURI());
        MDC.put("httpMethod", request.getMethod());
        MDC.put("appName", appName);

        // Try to get authentication from security context
        // Note: This may be null if authentication hasn't happened yet,
        // but JwtAuthenticationFilter will populate user details in MDC after authentication
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null) {
            String username = auth.getName();
            if (username != null && !username.equals("anonymousUser")) {
                MDC.put("userId", username);
            }

            // Extract token details if available (set by JwtAuthenticationFilter)
            // TokenDetails is stored in request attributes for backward compatibility
            TokenDetails tokenDetails = null;
            try {
                Object tokenDetailsObj = request.getAttribute("tokenDetails");
                if (tokenDetailsObj instanceof TokenDetails) {
                    tokenDetails = (TokenDetails) tokenDetailsObj;
                }
            } catch (Exception e) {
                // Ignore if not available
            }
            
            // Fallback to authentication details (for legacy support)
            if (tokenDetails == null) {
                Object details = auth.getDetails();
                if (details instanceof TokenDetails) {
                    tokenDetails = (TokenDetails) details;
                }
            }
            
            if (tokenDetails != null) {
                if (tokenDetails.tenantId() != null) {
                    MDC.put("tenantId", String.valueOf(tokenDetails.tenantId()));
                }
                if (tokenDetails.tenant() != null) {
                    MDC.put("tenant", String.valueOf(tokenDetails.tenant()));
                }
                if (tokenDetails.role() != null) {
                    MDC.put("role", tokenDetails.role());
                }
                if (tokenDetails.sessionId() != null) {
                    MDC.put("sessionId", tokenDetails.sessionId());
                }
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Clear MDC after request processing
            MDC.clear();
        }
    }
}
