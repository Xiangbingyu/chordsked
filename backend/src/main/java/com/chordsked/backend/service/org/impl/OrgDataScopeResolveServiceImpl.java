package com.chordsked.backend.service.org.impl;

import com.chordsked.backend.dao.OrgNodeDao;
import com.chordsked.backend.dao.UserOrgScopeDao;
import com.chordsked.backend.model.entity.OrgNodeEntity;
import com.chordsked.backend.model.entity.UserOrgScopeEntity;
import com.chordsked.backend.service.org.OrgDataScopeResolveService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

@Service("orgDataScopeResolveService")
public class OrgDataScopeResolveServiceImpl implements OrgDataScopeResolveService {
    @Resource(name = "userOrgScopeDao")
    private UserOrgScopeDao userOrgScopeDao;

    @Resource(name = "orgNodeDao")
    private OrgNodeDao orgNodeDao;

    @Override
    public OrgDataScopeResult resolveByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            return new OrgDataScopeResult(null, List.of(), List.of());
        }
        List<UserOrgScopeEntity> scopes = userOrgScopeDao.listByUserId(userId);
        if (scopes.isEmpty()) {
            return new OrgDataScopeResult(null, List.of(), List.of());
        }

        Long primaryOrgNodeId = scopes.stream()
                .filter(scope -> Integer.valueOf(1).equals(scope.getIsPrimary()))
                .map(UserOrgScopeEntity::getOrgNodeId)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        LinkedHashSet<Long> authorizedOrgNodeIds = new LinkedHashSet<>();
        LinkedHashSet<Long> authorizedCampusIds = new LinkedHashSet<>();
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
                if (node.getCampusId() != null && node.getCampusId() > 0) {
                    authorizedCampusIds.add(node.getCampusId());
                }
            }
        }
        return new OrgDataScopeResult(
                primaryOrgNodeId,
                List.copyOf(authorizedOrgNodeIds),
                List.copyOf(authorizedCampusIds)
        );
    }
}
