package com.chordsked.backend.idempotent.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要防重复提交的接口。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    /**
     * 幂等锁过期时间（秒）。
     */
    long expireSeconds() default 3L;

    /**
     * 重复提交提示信息。
     */
    String message() default "请勿重复操作";
}
