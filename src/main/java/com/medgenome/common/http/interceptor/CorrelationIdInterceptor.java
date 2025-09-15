package com.medgenome.common.http.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * Interceptor for managing correlation IDs in HTTP requests.
 * Adds or propagates correlation IDs for distributed tracing.
 */
@Slf4j
@Component
@ConditionalOnWebApplication
@EnableConfigurationProperties(InterceptorProperties.class)
@ConditionalOnProperty(prefix = "common.interceptor", name = "correlation-id-enabled", havingValue = "true", matchIfMissing = true)
public class CorrelationIdInterceptor implements HandlerInterceptor, Ordered {

    private static final String CORRELATION_ID_MDC_KEY = "correlationId";
    
    private final InterceptorProperties properties;

    public CorrelationIdInterceptor(InterceptorProperties properties) {
        this.properties = properties;
        log.info("CorrelationIdInterceptor initialized with header: {}", properties.getCorrelationIdHeaderName());
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull  HttpServletResponse response,
                             @NonNull  Object handler) {
        String correlationId = request.getHeader(properties.getCorrelationIdHeaderName());
        
        if (StringUtils.isBlank(correlationId) && properties.isGenerateCorrelationIdIfMissing()) {
            correlationId = UUID.randomUUID().toString();
            log.debug("Generated correlation ID: {}", correlationId);
        }
        
        if (StringUtils.isNotBlank(correlationId)) {
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
            response.setHeader(properties.getCorrelationIdHeaderName(), correlationId);
            log.debug("Using correlation ID: {}", correlationId);
        }
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MDC.remove(CORRELATION_ID_MDC_KEY);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}