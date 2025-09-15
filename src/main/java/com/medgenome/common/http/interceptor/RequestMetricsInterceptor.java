package com.medgenome.common.http.interceptor;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Interceptor for collecting metrics about HTTP requests.
 * Records request duration, status codes, and other metrics.
 */
@Slf4j
@Component
@ConditionalOnWebApplication
@ConditionalOnClass(MeterRegistry.class)
@ConditionalOnProperty(prefix = "common.interceptor", name = "metrics-enabled", havingValue = "true", matchIfMissing = true)
public class RequestMetricsInterceptor implements HandlerInterceptor, Ordered {

    private static final String START_TIME_ATTRIBUTE = "requestStartTime";
    
    private final MeterRegistry meterRegistry;

    public RequestMetricsInterceptor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        log.info("RequestMetricsInterceptor initialized");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull  HttpServletResponse response,
                             @NonNull  Object handler) {
        request.setAttribute(START_TIME_ATTRIBUTE, Instant.now());
        return true;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler, ModelAndView modelAndView) {
        // Not used
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler, Exception ex) {
        Instant startTime = (Instant) request.getAttribute(START_TIME_ATTRIBUTE);
        if (startTime != null) {
            long durationMs = Duration.between(startTime, Instant.now()).toMillis();
            
            String uri = request.getRequestURI();
            String method = request.getMethod();
            int status = response.getStatus();
            
            Timer.builder("http.server.requests")
                    .tag("uri", uri)
                    .tag("method", method)
                    .tag("status", String.valueOf(status))
                    .tag("exception", ex != null ? ex.getClass().getSimpleName() : "None")
                    .register(meterRegistry)
                    .record(durationMs, TimeUnit.MILLISECONDS);
            
            log.debug("Request {} {} completed with status {} in {} ms", method, uri, status, durationMs);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}