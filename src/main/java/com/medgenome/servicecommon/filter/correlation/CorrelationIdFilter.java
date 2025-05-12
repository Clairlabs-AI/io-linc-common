package com.medgenome.servicecommon.filter.correlation;

import com.medgenome.servicecommon.context.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that extracts and manages correlation IDs.
 * Sets up correlation ID in RequestContext and MDC.
 */
public class CorrelationIdFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(CorrelationIdFilter.class);
    private static final String CORRELATION_ID_LOG_VAR_NAME = "correlationId";
    
    private final CorrelationIdProperties properties;

    /**
     * Creates a new CorrelationIdFilter.
     *
     * @param properties correlation ID properties
     */
    public CorrelationIdFilter(CorrelationIdProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String correlationId = extractCorrelationId(request);
        RequestContext.current().setCorrelationId(correlationId);
        MDC.put(CORRELATION_ID_LOG_VAR_NAME, correlationId);
        
        try {
            if (properties.isIncludeInResponse()) {
                response.setHeader(properties.getHeaderName(), correlationId);
            }
            
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(CORRELATION_ID_LOG_VAR_NAME);
        }
    }

    private String extractCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(properties.getHeaderName());
        
        if (!StringUtils.hasText(correlationId) && properties.isGenerateIfMissing()) {
            correlationId = generateCorrelationId();
            logger.debug("Generated new correlation ID: {}", correlationId);
        }
        
        return correlationId;
    }

    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
}