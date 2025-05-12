package com.medgenome.servicecommon.logging.aspect;

import com.medgenome.servicecommon.logging.annotations.Audit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Aspect
@Component
@ConditionalOnProperty(prefix = "logging.audit", name = "enabled", havingValue = "true")
public class AuditLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger("com.medgenome.audit");

    @Around("@annotation(com.medgenome.servicecommon.logging.annotations.Audit)")
    public Object logAudit(ProceedingJoinPoint joinPoint, Audit audit) throws Throwable {
        try {
            MDC.put("auditAction", audit.action());
            MDC.put("auditEntity", audit.entity());

            return joinPoint.proceed();
        } finally {
            logger.info("Audit action '{}' performed on '{}'", audit.action(), audit.entity());

            MDC.remove("auditAction");
            MDC.remove("auditEntity");
        }
    }
}

