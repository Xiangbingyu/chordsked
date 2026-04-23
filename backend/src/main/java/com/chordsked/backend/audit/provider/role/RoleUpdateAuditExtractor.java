package com.chordsked.backend.audit.provider.role;

import com.chordsked.backend.audit.model.AuditLogContext;
import com.chordsked.backend.audit.provider.AuditLogPayloadExtractor;
import com.chordsked.backend.audit.provider.support.AbstractAuditLogPayloadExtractor;
import com.chordsked.backend.model.dto.audit.AuditLogRecordRequest;
import com.chordsked.backend.model.dto.role.RoleUpdateRequest;
import org.springframework.stereotype.Component;

@Component("roleUpdateAuditExtractor")
public class RoleUpdateAuditExtractor extends AbstractAuditLogPayloadExtractor implements AuditLogPayloadExtractor {
    @Override
    public String routeKey() {
        return AuditLogPayloadExtractor.buildRouteKey("ROLE_MANAGEMENT", "UPDATE_ROLE");
    }

    @Override
    public boolean supports(AuditLogContext context) {
        return findArg(context.getArgs(), RoleUpdateRequest.class) != null;
    }

    @Override
    public void fillOnSuccess(AuditLogRecordRequest request, AuditLogContext context) {
        RoleUpdateRequest updateRequest = findArg(context.getArgs(), RoleUpdateRequest.class);
        if (updateRequest == null) {
            fillStandardSuccess(request, "ROLE_UPDATE", null, null, null);
            return;
        }
        String input = "{\"roleId\":"
                + updateRequest.getRoleId()
                + ",\"name\":\""
                + updateRequest.getName()
                + "\",\"permissionIds\":"
                + updateRequest.getPermissionIds()
                + "}";
        String summary = "{\"status\":"
                + updateRequest.getStatus()
                + ",\"permissionCount\":"
                + safeSize(updateRequest.getPermissionIds())
                + "}";
        fillStandardSuccess(request, "ROLE_UPDATE", updateRequest.getRoleId(), input, summary);
    }

    @Override
    public void fillOnFailure(AuditLogRecordRequest request, AuditLogContext context, RuntimeException exception) {
        RoleUpdateRequest updateRequest = findArg(context.getArgs(), RoleUpdateRequest.class);
        Long bizId = updateRequest == null ? null : updateRequest.getRoleId();
        String input = updateRequest == null
                ? null
                : "{\"roleId\":" + updateRequest.getRoleId() + ",\"name\":\"" + updateRequest.getName() + "\",\"status\":" + updateRequest.getStatus() + "}";
        fillStandardFailure(request, "ROLE_UPDATE", bizId, input);
    }
}
