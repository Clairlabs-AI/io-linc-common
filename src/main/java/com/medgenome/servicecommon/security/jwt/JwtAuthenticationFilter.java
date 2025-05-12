package com.medgenome.servicecommon.security.jwt;

import com.medgenome.servicecommon.context.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * Filter to authenticate users with JWT token.
 * Extracts JWT from the request header and sets up the security context.
 * Also populates the RequestContext with user information.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtTokenProvider tokenProvider;
    private final JwtProperties jwtProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * Creates a new JwtAuthenticationFilter.
     *
     * @param tokenProvider JWT token provider
     * @param jwtProperties JWT configuration properties
     */
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, JwtProperties jwtProperties) {
        this.tokenProvider = tokenProvider;
        this.jwtProperties = jwtProperties;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = resolveToken(request);
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                Authentication auth = tokenProvider.getAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(auth);
                
                // Update RequestContext with user information
                if (auth.getPrincipal() instanceof UserDetails userDetails) {
                    RequestContext.current()
                        .setUsername(userDetails.getUsername())
                        .setUserId(tokenProvider.getUserId(jwt));
                }
                
                logger.debug("Set Authentication to security context for user: {}", 
                    auth.getName());
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return Arrays.stream(jwtProperties.getExcludedPaths())
            .anyMatch(p -> pathMatcher.match(p, path));
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(jwtProperties.getHeaderName());
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(jwtProperties.getTokenPrefix())) {
            return bearerToken.substring(jwtProperties.getTokenPrefix().length());
        }
        return null;
    }
}