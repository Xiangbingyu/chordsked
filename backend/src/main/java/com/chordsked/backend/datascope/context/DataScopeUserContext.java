package com.chordsked.backend.datascope.context;

import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.model.enums.UserDataScopeType;

import java.util.List;

public class DataScopeUserContext {
    private final Long userId;
    private final AccountUserType userType;
    private final UserDataScopeType dataScopeType;
    private final Long primaryOrgNodeId;
    private final List<Long> authorizedOrgNodeIds;
    private final List<Long> authorizedCampusIds;

    public DataScopeUserContext(
            Long userId,
            AccountUserType userType,
            UserDataScopeType dataScopeType,
            Long primaryOrgNodeId,
            List<Long> authorizedOrgNodeIds,
            List<Long> authorizedCampusIds
    ) {
        this.userId = userId;
        this.userType = userType;
        this.dataScopeType = dataScopeType;
        this.primaryOrgNodeId = primaryOrgNodeId;
        this.authorizedOrgNodeIds = authorizedOrgNodeIds == null ? List.of() : List.copyOf(authorizedOrgNodeIds);
        this.authorizedCampusIds = authorizedCampusIds == null ? List.of() : List.copyOf(authorizedCampusIds);
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

    public Long getPrimaryOrgNodeId() {
        return primaryOrgNodeId;
    }

    public List<Long> getAuthorizedOrgNodeIds() {
        return authorizedOrgNodeIds;
    }

    public List<Long> getAuthorizedCampusIds() {
        return authorizedCampusIds;
    }
}
