package com.chordsked.backend.audit.provider.internaluser;

import com.chordsked.backend.audit.model.AuditLogContext;
import com.chordsked.backend.audit.provider.AuditLogPayloadExtractor;
import com.chordsked.backend.audit.provider.support.AbstractAuditLogPayloadExtractor;
import com.chordsked.backend.model.dto.audit.AuditLogRecordRequest;
import com.chordsked.backend.model.dto.internaluser.InternalUserCreateRequest;
import org.springframework.stereotype.Component;

@Component("internalUserCreateAuditExtractor")
public class InternalUserCreateAuditExtractor extends AbstractAuditLogPayloadExtractor implements AuditLogPayloadExtractor {
    @Override
    public String routeKey() {
        return AuditLogPayloadExtractor.buildRouteKey("INTERNAL_USER_MANAGEMENT", "CREATE_INTERNAL_USER");
    }

    @Override
    public boolean supports(AuditLogContext context) {
        return findArg(context.getArgs(), InternalUserCreateRequest.class) != null;
    }

    @Override
    public void fillOnSuccess(AuditLogRecordRequest request, AuditLogContext context) {
        Long bizId = context.getResult() instanceof Number number ? number.longValue() : null;
        InternalUserCreateRequest createRequest = findArg(context.getArgs(), InternalUserCreateRequest.class);
        if (createRequest == null) {
            fillStandardSuccess(request, "INTERNAL_USER_CREATE", bizId, null, null);
            return;
        }
        String input = "{\"username\":\""
                + createRequest.getUsername()
                + "\",\"roleIds\":"
                + createRequest.getRoleIds()
                + ",\"primaryOrgNodeId\":"
                + createRequest.getPrimaryOrgNodeId()
                + ",\"orgScopeNodeIds\":"
                + createRequest.getOrgScopeNodeIds()
                + "}";
        String summary = "{\"roleCount\":"
                + safeSize(createRequest.getRoleIds())
                + ",\"orgScopeCount\":"
                + safeSize(createRequest.getOrgScopeNodeIds())
                + "}";
        fillStandardSuccess(request, "INTERNAL_USER_CREATE", bizId, input, summary);
    }

    @Override
    public void fillOnFailure(AuditLogRecordRequest request, AuditLogContext context, RuntimeException exception) {
        InternalUserCreateRequest createRequest = findArg(context.getArgs(), InternalUserCreateRequest.class);
        if (createRequest == null) {
            fillStandardFailure(request, "INTERNAL_USER_CREATE", null, null);
            return;
        }
        String input = "{\"username\":\""
                + createRequest.getUsername()
                + "\",\"phone\":\""
                + createRequest.getPhone()
                + "\",\"name\":\""
                + createRequest.getName()
                + "\"}";
        fillStandardFailure(request, "INTERNAL_USER_CREATE", null, input);
    }
}
