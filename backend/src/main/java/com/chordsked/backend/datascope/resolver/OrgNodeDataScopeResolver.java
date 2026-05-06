package com.chordsked.backend.datascope.resolver;

import com.chordsked.backend.dao.OrgNodeDao;
import com.chordsked.backend.dao.UserOrgScopeDao;
import com.chordsked.backend.model.entity.OrgNodeEntity;
import com.chordsked.backend.model.entity.UserOrgScopeEntity;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

@Component("orgNodeDataScopeResolver")
public class OrgNodeDataScopeResolver {
    @Resource(name = "userOrgScopeDao")
    private UserOrgScopeDao userOrgScopeDao;

    @Resource(name = "orgNodeDao")
    private OrgNodeDao orgNodeDao;

    public OrgNodeDataScopeResult resolveByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            return new OrgNodeDataScopeResult(null, List.of());
        }
        List<UserOrgScopeEntity> scopes = userOrgScopeDao.listByUserId(userId);
        if (scopes.isEmpty()) {
            return new OrgNodeDataScopeResult(null, List.of());
        }

        Long primaryOrgNodeId = scopes.stream()
                .filter(scope -> Integer.valueOf(1).equals(scope.getIsPrimary()))
                .map(UserOrgScopeEntity::getOrgNodeId)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        LinkedHashSet<Long> authorizedOrgNodeIds = new LinkedHashSet<>();
        for (UserOrgScopeEntity scope : scopes) {
            Long orgNodeId = scope.getOrgNodeId();
            if (orgNodeId == null || orgNodeId <= 0) {
                continue;
            }
            List<OrgNodeEntity> descendants = orgNodeDao.listDescendantsByNodeId(orgNodeId);
            for (OrgNodeEntity node : descendants) {
                if (node.getId() != null && node.getId() > 0) {
                    authorizedOrgNodeIds.add(node.getId());
                }
            }
        }
        return new OrgNodeDataScopeResult(
                primaryOrgNodeId,
                List.copyOf(authorizedOrgNodeIds)
        );
    }

    public record OrgNodeDataScopeResult(
            Long primaryOrgNodeId,
            List<Long> authorizedOrgNodeIds
    ) {
    }
}
