package com.chordsked.backend.audit.model;

import java.lang.reflect.Method;

/**
 * 审计日志提取上下文，封装切面可见的方法与参数信息。
 */
public class AuditLogContext {
    private final Method method;
    private final Object[] args;
    private final Object result;

    public AuditLogContext(Method method, Object[] args, Object result) {
        this.method = method;
        this.args = args == null ? new Object[0] : args.clone();
        this.result = result;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args.clone();
    }

    public Object getResult() {
        return result;
    }
}
