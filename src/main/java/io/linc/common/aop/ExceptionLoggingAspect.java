package io.linc.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * AOP aspect for logging exceptions.
 * Applied to all methods in the application.
 */
@Aspect
@Component
@Slf4j
@ConditionalOnProperty(prefix = "common.aop.exception", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ExceptionLoggingAspect {

    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void serviceLayer() {
    }
    
    @Pointcut("within(@org.springframework.stereotype.Repository *)")
    public void repositoryLayer() {
    }
    
    @Pointcut("within(@org.springframework.stereotype.Controller *)")
    public void controllerLayer() {
    }
    
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restControllerLayer() {
    }
    
    @Pointcut("serviceLayer() || repositoryLayer() || controllerLayer() || restControllerLayer()")
    public void applicationLayer() {
    }

    @AfterThrowing(pointcut = "applicationLayer()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        log.error("Exception in {}.{}() with cause = {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                ex.getMessage() != null ? ex.getMessage() : "NULL");
        
        // Log stack trace for non-business exceptions
        if (!(ex instanceof RuntimeException)) {
            log.error("Stack trace:", ex);
        }
    }
}