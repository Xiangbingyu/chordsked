package com.chordsked.backend.datascope.strategy;

import com.chordsked.backend.datascope.context.DataScopeUserContext;
import com.chordsked.backend.model.enums.UserDataScopeType;

public interface DataScopeStrategy {
    UserDataScopeType getDataScopeType();

    String buildCondition(DataScopeUserContext userContext, String tableAlias, String scopeField);
}
