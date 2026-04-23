package com.chordsked.backend.audit.provider.internaluser;

import com.chordsked.backend.audit.model.AuditLogContext;
import com.chordsked.backend.audit.provider.AuditLogPayloadExtractor;
import com.chordsked.backend.audit.provider.support.AbstractAuditLogPayloadExtractor;
import com.chordsked.backend.model.dto.audit.AuditLogRecordRequest;
import com.chordsked.backend.model.dto.internaluser.InternalUserResetPasswordRequest;
import org.springframework.stereotype.Component;

@Component("internalUserResetPasswordAuditExtractor")
public class InternalUserResetPasswordAuditExtractor extends AbstractAuditLogPayloadExtractor implements AuditLogPayloadExtractor {
    @Override
    public String routeKey() {
        return AuditLogPayloadExtractor.buildRouteKey("INTERNAL_USER_MANAGEMENT", "RESET_INTERNAL_USER_PASSWORD");
    }

    @Override
    public boolean supports(AuditLogContext context) {
        return findArg(context.getArgs(), InternalUserResetPasswordRequest.class) != null;
    }

    @Override
    public void fillOnSuccess(AuditLogRecordRequest request, AuditLogContext context) {
        InternalUserResetPasswordRequest resetRequest = findArg(context.getArgs(), InternalUserResetPasswordRequest.class);
        if (resetRequest == null) {
            fillStandardSuccess(request, "INTERNAL_USER_RESET_PASSWORD", null, null, null);
            return;
        }
        String input = "{\"userId\":"
                + resetRequest.getUserId()
                + ",\"reason\":\""
                + (resetRequest.getReason() == null ? "" : resetRequest.getReason())
                + "\"}";
        String summary = "{\"passwordReset\":true}";
        fillStandardSuccess(request, "INTERNAL_USER_RESET_PASSWORD", resetRequest.getUserId(), input, summary);
    }

    @Override
    public void fillOnFailure(AuditLogRecordRequest request, AuditLogContext context, RuntimeException exception) {
        InternalUserResetPasswordRequest resetRequest = findArg(context.getArgs(), InternalUserResetPasswordRequest.class);
        Long bizId = resetRequest == null ? null : resetRequest.getUserId();
        String input = resetRequest == null
                ? null
                : "{\"userId\":" + resetRequest.getUserId() + "}";
        fillStandardFailure(request, "INTERNAL_USER_RESET_PASSWORD", bizId, input);
    }
}
