package com.chordsked.backend.audit.provider;

import com.chordsked.backend.audit.model.AuditLogContext;
import com.chordsked.backend.model.dto.audit.AuditLogRecordRequest;

/**
 * 审计日志载荷提取器，由各业务模块按需实现。
 */
public interface AuditLogPayloadExtractor {
    /**
     * 提取器路由键，格式为 module:action。
     */
    String routeKey();

    /**
     * 是否支持当前调用上下文。
     */
    boolean supports(AuditLogContext context);

    /**
     * 填充成功审计字段。
     */
    void fillOnSuccess(AuditLogRecordRequest request, AuditLogContext context);

    /**
     * 填充失败审计字段。
     */
    void fillOnFailure(AuditLogRecordRequest request, AuditLogContext context, RuntimeException exception);

    static String buildRouteKey(String module, String action) {
        return (module == null ? "" : module.trim()) + ":" + (action == null ? "" : action.trim());
    }
}
