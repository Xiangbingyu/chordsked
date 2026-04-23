package com.chordsked.backend.datascope.strategy;

import com.chordsked.backend.datascope.context.DataScopeUserContext;
import com.chordsked.backend.model.enums.UserDataScopeType;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component("campusDataScopeStrategy")
public class CampusDataScopeStrategy implements DataScopeStrategy {
    @Override
    public UserDataScopeType getDataScopeType() {
        return UserDataScopeType.SPECIFIED_CAMPUS;
    }

    @Override
    public String buildCondition(DataScopeUserContext userContext, String tableAlias, String scopeField) {
        if (userContext.getCampusIds().isEmpty()) {
            return "1 = 0";
        }
        String campusIdList = userContext.getCampusIds().stream()
                .filter(campusId -> campusId != null && campusId > 0)
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
        if (campusIdList.isEmpty()) {
            return "1 = 0";
        }
        return "EXISTS (SELECT 1 FROM sys_user_campus ds_uc WHERE ds_uc.user_id = "
                + tableAlias + "." + scopeField
                + " AND ds_uc.campus_id IN (" + campusIdList + "))";
    }
}
