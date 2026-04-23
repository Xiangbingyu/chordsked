package com.chordsked.backend.audit.provider.support;

import com.chordsked.backend.model.dto.audit.AuditLogRecordRequest;

/**
 * 审计提取器公共模板，统一 requestParams/responseResult 的字段结构。
 */
public abstract class AbstractAuditLogPayloadExtractor {
    protected <T> T findArg(Object[] args, Class<T> type) {
        if (args == null || args.length == 0 || type == null) {
            return null;
        }
        for (Object arg : args) {
            if (type.isInstance(arg)) {
                return type.cast(arg);
            }
        }
        return null;
    }

    protected void fillStandardSuccess(
            AuditLogRecordRequest request,
            String bizType,
            Long bizId,
            String inputJson,
            String summaryJson
    ) {
        request.setBizId(bizId);
        request.setRequestParams(
                "{\"bizType\":\""
                        + safe(bizType)
                        + "\",\"bizId\":"
                        + nullableNumber(bizId)
                        + ",\"input\":"
                        + objectOrEmpty(inputJson)
                        + "}"
        );
        request.setResponseResult(
                "{\"success\":true,\"summary\":"
                        + objectOrEmpty(summaryJson)
                        + "}"
        );
    }

    protected void fillStandardFailure(
            AuditLogRecordRequest request,
            String bizType,
            Long bizId,
            String inputJson
    ) {
        request.setBizId(bizId);
        request.setRequestParams(
                "{\"bizType\":\""
                        + safe(bizType)
                        + "\",\"bizId\":"
                        + nullableNumber(bizId)
                        + ",\"input\":"
                        + objectOrEmpty(inputJson)
                        + "}"
        );
        request.setResponseResult("{\"success\":false}");
    }

    protected int safeSize(java.util.List<?> values) {
        return values == null ? 0 : values.size();
    }

    protected String nullableNumber(Long value) {
        return value == null ? "null" : value.toString();
    }

    protected String safe(String value) {
        return value == null ? "" : value;
    }

    protected String objectOrEmpty(String jsonObject) {
        return jsonObject == null || jsonObject.isBlank() ? "{}" : jsonObject;
    }
}
