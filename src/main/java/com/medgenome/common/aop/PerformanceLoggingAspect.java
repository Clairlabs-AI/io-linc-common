package com.medgenome.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * AOP aspect for logging method execution time.
 * Applied to methods annotated with @LogPerformance.
 */
@Aspect
@Component
@Slf4j
@ConditionalOnProperty(prefix = "common.aop.performance", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PerformanceLoggingAspect {

    @Pointcut("@annotation(com.medgenome.common.aop.annotation.LogPerformance)")
    public void performanceLoggingPointcut() {
    }

    @Around("performanceLoggingPointcut()")
    public Object logMethodExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = method.getName();
        String arguments = Arrays.stream(joinPoint.getArgs())
                .map(arg -> arg != null ? arg.toString() : "null")
                .collect(Collectors.joining(", "));
        
        log.debug("Executing: {}.{}({})", className, methodName, arguments);
        
        Instant start = Instant.now();
        Object result = joinPoint.proceed();
        Duration duration = Duration.between(start, Instant.now());
        
        log.info("Executed: {}.{} - Duration: {} ms", className, methodName, duration.toMillis());
        
        return result;
    }
}