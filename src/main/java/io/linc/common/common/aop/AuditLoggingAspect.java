package io.linc.common.common.aop;

import io.linc.common.common.aop.annotation.Audited;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * AOP aspect for audit logging.
 * Applied to methods annotated with @Audited.
 */
@Aspect
@Component
@ConditionalOnProperty(prefix = "common.aop.audit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuditLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLoggingAspect.class);

    @Pointcut("@annotation(com.medgenome.common.aop.annotation.Audited)")
    public void auditPointcut() {
    }

    @AfterReturning(pointcut = "auditPointcut()", returning = "result")
    public void logAuditEvent(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Audited auditedAnnotation = method.getAnnotation(Audited.class);
        
        String action = auditedAnnotation.action();
        if (action.isEmpty()) {
            action = method.getName();
        }
        
        String username = getCurrentUsername();
        String targetClass = joinPoint.getTarget().getClass().getSimpleName();
        String targetMethod = method.getName();
        
        log.info("AUDIT: User '{}' performed '{}' on {}.{} at {}", 
                username, action, targetClass, targetMethod, LocalDateTime.now());
        
        // thinking of persist audit events to a database or send to external system
    }
    
    private String getCurrentUsername() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName)
                .orElse("anonymousUser");
    }
}