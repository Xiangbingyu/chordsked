package com.chordsked.backend.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要自动记录审计日志的方法。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {
    /**
     * 审计模块编码，例如 INTERNAL_USER_MANAGEMENT。
     */
    String module();

    /**
     * 审计动作编码，例如 CREATE_INTERNAL_USER。
     */
    String action();
}
