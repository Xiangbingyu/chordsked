package com.chordsked.backend.datascope.context;

import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.model.enums.UserDataScopeType;

import java.util.List;

public class DataScopeUserContext {
    private final Long userId;
    private final AccountUserType userType;
    private final UserDataScopeType dataScopeType;
    private final List<Long> campusIds;

    public DataScopeUserContext(
            Long userId,
            AccountUserType userType,
            UserDataScopeType dataScopeType,
            List<Long> campusIds
    ) {
        this.userId = userId;
        this.userType = userType;
        this.dataScopeType = dataScopeType;
        this.campusIds = campusIds == null ? List.of() : List.copyOf(campusIds);
    }

    public Long getUserId() {
        return userId;
    }

    public AccountUserType getUserType() {
        return userType;
    }

    public UserDataScopeType getDataScopeType() {
        return dataScopeType;
    }

    public List<Long> getCampusIds() {
        return campusIds;
    }
}
