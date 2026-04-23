package com.chordsked.backend.audit.provider.role;

import com.chordsked.backend.audit.model.AuditLogContext;
import com.chordsked.backend.audit.provider.AuditLogPayloadExtractor;
import com.chordsked.backend.audit.provider.support.AbstractAuditLogPayloadExtractor;
import com.chordsked.backend.model.dto.audit.AuditLogRecordRequest;
import com.chordsked.backend.model.dto.role.RoleCreateRequest;
import org.springframework.stereotype.Component;

@Component("roleCreateAuditExtractor")
public class RoleCreateAuditExtractor extends AbstractAuditLogPayloadExtractor implements AuditLogPayloadExtractor {
    @Override
    public String routeKey() {
        return AuditLogPayloadExtractor.buildRouteKey("ROLE_MANAGEMENT", "CREATE_ROLE");
    }

    @Override
    public boolean supports(AuditLogContext context) {
        return findArg(context.getArgs(), RoleCreateRequest.class) != null;
    }

    @Override
    public void fillOnSuccess(AuditLogRecordRequest request, AuditLogContext context) {
        Long bizId = context.getResult() instanceof Number number ? number.longValue() : null;
        RoleCreateRequest createRequest = findArg(context.getArgs(), RoleCreateRequest.class);
        if (createRequest == null) {
            fillStandardSuccess(request, "ROLE_CREATE", bizId, null, null);
            return;
        }
        String input = "{\"code\":\""
                + createRequest.getCode()
                + "\",\"name\":\""
                + createRequest.getName()
                + "\",\"permissionIds\":"
                + createRequest.getPermissionIds()
                + "}";
        String summary = "{\"status\":"
                + createRequest.getStatus()
                + ",\"permissionCount\":"
                + safeSize(createRequest.getPermissionIds())
                + "}";
        fillStandardSuccess(request, "ROLE_CREATE", bizId, input, summary);
    }

    @Override
    public void fillOnFailure(AuditLogRecordRequest request, AuditLogContext context, RuntimeException exception) {
        RoleCreateRequest createRequest = findArg(context.getArgs(), RoleCreateRequest.class);
        String input = createRequest == null
                ? null
                : "{\"code\":\"" + createRequest.getCode() + "\",\"name\":\"" + createRequest.getName() + "\",\"status\":" + createRequest.getStatus() + "}";
        fillStandardFailure(request, "ROLE_CREATE", null, input);
    }
}
