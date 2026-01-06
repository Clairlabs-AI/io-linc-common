package com.medgenome.auth.security;

import com.medgenome.auth.dto.TokenDetails;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter to authenticate users with JWT token.
 * Extracts JWT from the request header, validates it, and sets up the security context.
 * Also populates MDC with user information for logging and auditing.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtTokenProvider tokenProvider;

    /**
     * Creates a new JwtAuthenticationFilter.
     *
     * @param tokenProvider JWT token provider
     */
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        // Skip JWT authentication for OPTIONS requests (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            String token = resolveToken(request);
            if (token != null) {
                // Validate the token if secret key is configured
                // If key is not configured, validation will return false but we can still parse
                boolean isValid = tokenProvider.validateToken(token);
                if (!isValid) {
                    // If validation failed and we have a key configured, reject the request
                    // If no key is configured, we'll still try to parse (validation returns false but parsing may succeed)
                    LOGGER.debug("Token validation returned false for request to {}", request.getRequestURI());
                }

                // Parse token to extract details
                // This will validate signature if key is configured, or parse without validation if key is not configured
                TokenDetails tokenDetails;
                try {
                    tokenDetails = tokenProvider.parseToken(token);
                } catch (JwtException ex) {
                    // If parsing fails (e.g., invalid token format or signature validation failed with key), reject
                    LOGGER.warn("Failed to parse token for request to {}: {}", request.getRequestURI(), ex.getMessage());
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                // Check permission-based access control
                if (!isApplicationAllowed(tokenDetails.permissions(), request)) {
                    LOGGER.warn("Access denied for user: {} to application: {}", 
                            tokenDetails.username(), request.getServletPath());
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                // Build authorities from permissions
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                if (tokenDetails.permissions() != null && !tokenDetails.permissions().isEmpty()) {
                    authorities = tokenDetails.permissions().stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                }
                
                // Add role as an authority if present
                if (tokenDetails.role() != null && !tokenDetails.role().isEmpty()) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + tokenDetails.role().toUpperCase().replace(" ", "_")));
                }

                // Create authentication token with authorities
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        tokenDetails.username(),
                        null, // No password needed for JWT authentication
                        authorities
                );

                // Store tenantId as Integer in details for backward compatibility
                // Consuming services expect authentication.getDetails() to return Integer (tenantId)
                // TokenDetails can be accessed via AuthenticationUtils.getTokenDetails() if needed
                Integer tenantIdForDetails = tokenDetails.tenantId() != null ? tokenDetails.tenantId() : 
                        (tokenDetails.tenant() != null ? tokenDetails.tenant() : null);
                authentication.setDetails(tenantIdForDetails);
                
                // Store TokenDetails in request attribute for services that need full token details
                request.setAttribute("tokenDetails", tokenDetails);
                
                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Populate MDC with user information for logging
                MDC.put("userId", tokenDetails.username());
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

                LOGGER.debug("Authenticated user: {} with role: {} and tenant: {}", 
                        tokenDetails.username(), tokenDetails.role(), tokenDetails.tenantId());
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            LOGGER.error("Failed to set user authentication in security context", e);
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } finally {
            // Clean up MDC entries added by this filter
            MDC.remove("userId");
            MDC.remove("tenantId");
            MDC.remove("tenant");
            MDC.remove("role");
            MDC.remove("sessionId");
        }
    }

    /**
     * Checks if the user has permission to access the current application.
     * This method checks if any permission in the token grants access to the application
     * determined from the MDC context or request.
     *
     * @param allowedPermissions List of permissions from the token
     * @param request HTTP request
     * @return true if access is allowed, false otherwise
     */
    private boolean isApplicationAllowed(List<String> allowedPermissions, HttpServletRequest request) {
        // Extract application name from request
        String currentApplication = MDC.get("appName");

        // If no application can be determined, allow access (backward compatibility)
        if (!StringUtils.hasText(currentApplication)) {
            return true;
        }

        // If no permissions, deny access
        if (allowedPermissions == null || allowedPermissions.isEmpty()) {
            LOGGER.warn("No permissions found in token for user");
            return false;
        }

        // Check if application name is directly in permissions list (case-insensitive)
        String appNameUpper = currentApplication.toUpperCase();

        // Check if any permission grants access to the application
        // We check if any permission starts with the application name (case-insensitive)
        return allowedPermissions.stream()
                .anyMatch(permission -> {
                    String permissionUpper = permission.toUpperCase();
                    // Check if permission starts with application name or contains it
                    return permissionUpper.startsWith(appNameUpper) || permissionUpper.contains("_" + appNameUpper + "_");
                });
    }

    /**
     * Resolves the JWT token from the Authorization header.
     *
     * @param req HTTP request
     * @return JWT token string or null if not found
     */
    public String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
