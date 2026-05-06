package com.chordsked.backend.datascope.strategy;

import com.chordsked.backend.datascope.context.DataScopeUserContext;
import com.chordsked.backend.model.enums.UserDataScopeType;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component("assignedDataScopeStrategy")
public class AssignedDataScopeStrategy implements DataScopeStrategy {
    @Override
    public UserDataScopeType getDataScopeType() {
        return UserDataScopeType.ASSIGNED;
    }

    @Override
    public String buildCondition(DataScopeUserContext userContext, String tableAlias, String scopeField) {
        if (scopeField == null || scopeField.isBlank()) {
            return "1 = 0";
        }
        if ("org_node_id".equals(scopeField)) {
            String orgNodeIdList = userContext.getAuthorizedOrgNodeIds().stream()
                    .filter(orgNodeId -> orgNodeId != null && orgNodeId > 0)
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            if (orgNodeIdList.isEmpty()) {
                return "1 = 0";
            }
            return tableAlias + "." + scopeField + " IN (" + orgNodeIdList + ")";
        }
        if ("id".equals(scopeField)) {
            String orgNodeIdList = userContext.getAuthorizedOrgNodeIds().stream()
                    .filter(orgNodeId -> orgNodeId != null && orgNodeId > 0)
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            if (orgNodeIdList.isEmpty()) {
                return "1 = 0";
            }
            return "EXISTS (SELECT 1 FROM sys_user_org_scope ds_uos WHERE ds_uos.user_id = "
                    + tableAlias + "." + scopeField
                    + " AND ds_uos.org_node_id IN (" + orgNodeIdList + "))";
        }
        return "1 = 0";
    }
}
