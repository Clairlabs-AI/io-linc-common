package com.medgenome.common.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods for audit logging.
 * Methods annotated with @Audited will generate audit log entries.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
    /**
     * The action being performed, defaults to method name if not specified.
     */
    String action() default "";
    
    /**
     * The domain/entity the action applies to.
     */
    String domain() default "";
}