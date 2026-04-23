package com.chordsked.backend.datascope.strategy;

import com.chordsked.backend.datascope.context.DataScopeUserContext;
import com.chordsked.backend.model.enums.UserDataScopeType;
import org.springframework.stereotype.Component;

@Component("selfDataScopeStrategy")
public class SelfDataScopeStrategy implements DataScopeStrategy {
    @Override
    public UserDataScopeType getDataScopeType() {
        return UserDataScopeType.SELF_ONLY;
    }

    @Override
    public String buildCondition(DataScopeUserContext userContext, String tableAlias, String scopeField) {
        return tableAlias + "." + scopeField + " = " + userContext.getUserId();
    }
}
