package com.chordsked.backend.audit.provider.internaluser;

import com.chordsked.backend.audit.model.AuditLogContext;
import com.chordsked.backend.audit.provider.AuditLogPayloadExtractor;
import com.chordsked.backend.audit.provider.support.AbstractAuditLogPayloadExtractor;
import com.chordsked.backend.model.dto.audit.AuditLogRecordRequest;
import com.chordsked.backend.model.dto.internaluser.InternalUserUpdateRequest;
import org.springframework.stereotype.Component;

@Component("internalUserUpdateAuditExtractor")
public class InternalUserUpdateAuditExtractor extends AbstractAuditLogPayloadExtractor implements AuditLogPayloadExtractor {
    @Override
    public String routeKey() {
        return AuditLogPayloadExtractor.buildRouteKey("INTERNAL_USER_MANAGEMENT", "UPDATE_INTERNAL_USER");
    }

    @Override
    public boolean supports(AuditLogContext context) {
        return findArg(context.getArgs(), InternalUserUpdateRequest.class) != null;
    }

    @Override
    public void fillOnSuccess(AuditLogRecordRequest request, AuditLogContext context) {
        InternalUserUpdateRequest updateRequest = findArg(context.getArgs(), InternalUserUpdateRequest.class);
        if (updateRequest == null) {
            fillStandardSuccess(request, "INTERNAL_USER_UPDATE", null, null, null);
            return;
        }
        String input = "{\"userId\":"
                + updateRequest.getUserId()
                + ",\"phone\":\""
                + updateRequest.getPhone()
                + "\",\"roleIds\":"
                + updateRequest.getRoleIds()
                + ",\"campusIds\":"
                + updateRequest.getCampusIds()
                + "}";
        String summary = "{\"dataScopeType\":"
                + updateRequest.getDataScopeType()
                + ",\"roleCount\":"
                + safeSize(updateRequest.getRoleIds())
                + ",\"campusCount\":"
                + safeSize(updateRequest.getCampusIds())
                + "}";
        fillStandardSuccess(request, "INTERNAL_USER_UPDATE", updateRequest.getUserId(), input, summary);
    }

    @Override
    public void fillOnFailure(AuditLogRecordRequest request, AuditLogContext context, RuntimeException exception) {
        InternalUserUpdateRequest updateRequest = findArg(context.getArgs(), InternalUserUpdateRequest.class);
        Long bizId = updateRequest == null ? null : updateRequest.getUserId();
        String input = updateRequest == null
                ? null
                : "{\"userId\":" + updateRequest.getUserId() + ",\"phone\":\"" + updateRequest.getPhone() + "\",\"name\":\"" + updateRequest.getName() + "\"}";
        fillStandardFailure(request, "INTERNAL_USER_UPDATE", bizId, input);
    }
}
