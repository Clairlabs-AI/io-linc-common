package com.medgenome.auth.security;

import com.medgenome.auth.dto.TokenDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Filter to authenticate users with JWT token.
 * Extracts JWT from the request header and sets up the security context.
 * Also populates the RequestContext with user information.
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
                                    @NonNull FilterChain filterChain) {
        try {
            String token = resolveToken(request);
            if (token != null && tokenProvider.validateToken(token)) {
                TokenDetails tokenDetails = tokenProvider.parseToken(token);

                if (!isApplicationAllowed(tokenDetails.allowedModules(), request)) {
                    LOGGER.warn("Access denied for user: {} to application: {}", tokenDetails.username(), request.getServletPath());
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        tokenDetails.username(),
                        tokenDetails.encodedPassword(),
                        new ArrayList<>() // authenticated = true, no roles
                );

                authentication.setDetails(tokenDetails.tenantId());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            LOGGER.error("Failed to set user authentication in security context", e);
            SecurityContextHolder.clearContext();
        }

    }

    private boolean isApplicationAllowed(List<String> allowedApplications, HttpServletRequest request) {
        String currentApplication =  MDC.get("appName");
        if (CollectionUtils.isEmpty(allowedApplications)) {
            allowedApplications = new ArrayList<>();
            //allowedApplications.addAll(Arrays.asList("Patient", "Sample"));
            allowedApplications.addAll(Arrays.asList("Patient", "Sample","Family","BulkImport"));

        }
        return allowedApplications.contains(currentApplication);
    }

    public String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

}