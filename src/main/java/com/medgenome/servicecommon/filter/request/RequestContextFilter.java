package com.medgenome.servicecommon.filter.request;

import com.medgenome.servicecommon.context.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that sets up and cleans up the RequestContext.
 * Ensures ThreadLocal is properly managed for each request.
 */
public class RequestContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String correlationId = request.getHeader("X-CorrelationId");
            String sessionId = request.getHeader("X-SessionId");
            String messageId = request.getHeader("X-MessageId");

            RequestContext.current()
                    .setMessageId(messageId)
                    .setCorrelationId(correlationId)
                    .setSessionId(sessionId);

            filterChain.doFilter(request, response);
        } finally {
            RequestContext.clear();
        }
    }
}