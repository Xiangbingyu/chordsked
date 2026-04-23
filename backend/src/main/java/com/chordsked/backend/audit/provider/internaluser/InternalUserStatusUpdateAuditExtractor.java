package com.chordsked.backend.audit.provider.internaluser;

import com.chordsked.backend.audit.model.AuditLogContext;
import com.chordsked.backend.audit.provider.AuditLogPayloadExtractor;
import com.chordsked.backend.audit.provider.support.AbstractAuditLogPayloadExtractor;
import com.chordsked.backend.model.dto.audit.AuditLogRecordRequest;
import com.chordsked.backend.model.dto.internaluser.InternalUserStatusUpdateRequest;
import org.springframework.stereotype.Component;

@Component("internalUserStatusUpdateAuditExtractor")
public class InternalUserStatusUpdateAuditExtractor extends AbstractAuditLogPayloadExtractor implements AuditLogPayloadExtractor {
    @Override
    public String routeKey() {
        return AuditLogPayloadExtractor.buildRouteKey("INTERNAL_USER_MANAGEMENT", "UPDATE_INTERNAL_USER_STATUS");
    }

    @Override
    public boolean supports(AuditLogContext context) {
        return findArg(context.getArgs(), InternalUserStatusUpdateRequest.class) != null;
    }

    @Override
    public void fillOnSuccess(AuditLogRecordRequest request, AuditLogContext context) {
        InternalUserStatusUpdateRequest statusRequest = findArg(context.getArgs(), InternalUserStatusUpdateRequest.class);
        if (statusRequest == null) {
            fillStandardSuccess(request, "INTERNAL_USER_UPDATE_STATUS", null, null, null);
            return;
        }
        String input = "{\"userId\":"
                + statusRequest.getUserId()
                + ",\"status\":"
                + statusRequest.getStatus()
                + "}";
        String summary = "{\"statusUpdated\":true}";
        fillStandardSuccess(request, "INTERNAL_USER_UPDATE_STATUS", statusRequest.getUserId(), input, summary);
    }

    @Override
    public void fillOnFailure(AuditLogRecordRequest request, AuditLogContext context, RuntimeException exception) {
        InternalUserStatusUpdateRequest statusRequest = findArg(context.getArgs(), InternalUserStatusUpdateRequest.class);
        Long bizId = statusRequest == null ? null : statusRequest.getUserId();
        String input = statusRequest == null
                ? null
                : "{\"userId\":" + statusRequest.getUserId() + ",\"status\":" + statusRequest.getStatus() + "}";
        fillStandardFailure(request, "INTERNAL_USER_UPDATE_STATUS", bizId, input);
    }
}
