package com.medgenome.servicecommon.logging.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Aspect
@Component
@ConditionalOnProperty(prefix = "logging.performance", name = "enabled", havingValue = "true")
public class PerformanceLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger("com.medgenome.performance");

    @Around("@annotation(com.medgenome.servicecommon.logging.annotations.LogPerformance)")
    public Object logPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();

            MDC.put("methodName", signature.getMethod().getName());
            MDC.put("className", signature.getDeclaringType().getSimpleName());
            MDC.put("executionTime", String.valueOf(duration));

            logger.debug("Performance logged for method: {}", signature.toShortString());

            MDC.remove("methodName");
            MDC.remove("className");
            MDC.remove("executionTime");
        }
    }
}
