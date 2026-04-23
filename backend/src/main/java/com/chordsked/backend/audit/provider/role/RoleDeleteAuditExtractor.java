package com.chordsked.backend.audit.provider.role;

import com.chordsked.backend.audit.model.AuditLogContext;
import com.chordsked.backend.audit.provider.AuditLogPayloadExtractor;
import com.chordsked.backend.audit.provider.support.AbstractAuditLogPayloadExtractor;
import com.chordsked.backend.model.dto.audit.AuditLogRecordRequest;
import com.chordsked.backend.model.dto.role.RoleDeleteRequest;
import org.springframework.stereotype.Component;

@Component("roleDeleteAuditExtractor")
public class RoleDeleteAuditExtractor extends AbstractAuditLogPayloadExtractor implements AuditLogPayloadExtractor {
    @Override
    public String routeKey() {
        return AuditLogPayloadExtractor.buildRouteKey("ROLE_MANAGEMENT", "DELETE_ROLE");
    }

    @Override
    public boolean supports(AuditLogContext context) {
        return findArg(context.getArgs(), RoleDeleteRequest.class) != null;
    }

    @Override
    public void fillOnSuccess(AuditLogRecordRequest request, AuditLogContext context) {
        RoleDeleteRequest deleteRequest = findArg(context.getArgs(), RoleDeleteRequest.class);
        if (deleteRequest == null) {
            fillStandardSuccess(request, "ROLE_DELETE", null, null, null);
            return;
        }
        String input = "{\"roleId\":" + deleteRequest.getRoleId() + "}";
        String summary = "{\"deleted\":true}";
        fillStandardSuccess(request, "ROLE_DELETE", deleteRequest.getRoleId(), input, summary);
    }

    @Override
    public void fillOnFailure(AuditLogRecordRequest request, AuditLogContext context, RuntimeException exception) {
        RoleDeleteRequest deleteRequest = findArg(context.getArgs(), RoleDeleteRequest.class);
        Long roleId = deleteRequest == null ? null : deleteRequest.getRoleId();
        String input = deleteRequest == null ? null : "{\"roleId\":" + roleId + "}";
        fillStandardFailure(request, "ROLE_DELETE", roleId, input);
    }
}
