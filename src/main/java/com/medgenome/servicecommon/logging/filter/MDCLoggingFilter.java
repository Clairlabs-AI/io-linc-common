package com.medgenome.servicecommon.logging.filter;

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

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MDCLoggingFilter extends OncePerRequestFilter {

    @Value("${app.name}")
    private String appName;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws IOException, ServletException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        MDC.put("correlationId", request.getHeader("X-CorrelationId"));
        MDC.put("messageId", request.getHeader("X-MessageId"));
        MDC.put("clientIp", request.getRemoteAddr());
        MDC.put("requestPath", request.getRequestURI());
        MDC.put("httpMethod", request.getMethod());
        MDC.put("appName", appName);

        if (auth != null && auth.isAuthenticated()) {
            MDC.put("userId", auth.getName());
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}

