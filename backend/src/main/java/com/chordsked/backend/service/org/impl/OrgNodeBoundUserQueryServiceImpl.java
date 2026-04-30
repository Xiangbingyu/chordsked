package com.chordsked.backend.service.org.impl;

import com.chordsked.backend.dao.InternalUserDao;
import com.chordsked.backend.dao.UserOrgScopeDao;
import com.chordsked.backend.model.entity.InternalUserEntity;
import com.chordsked.backend.model.entity.UserOrgScopeEntity;
import com.chordsked.backend.model.vo.org.OrgNodeBoundUserVO;
import com.chordsked.backend.service.org.OrgNodeBoundUserQueryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("orgNodeBoundUserQueryService")
public class OrgNodeBoundUserQueryServiceImpl implements OrgNodeBoundUserQueryService {
    @Resource(name = "userOrgScopeDao")
    private UserOrgScopeDao userOrgScopeDao;

    @Resource(name = "internalUserDao")
    private InternalUserDao internalUserDao;

    @Override
    public List<OrgNodeBoundUserVO> listByNodeId(Long nodeId) {
        if (nodeId == null || nodeId <= 0) {
            return List.of();
        }
        List<UserOrgScopeEntity> scopes = userOrgScopeDao.listByOrgNodeId(nodeId);
        return scopes.stream()
                .map(UserOrgScopeEntity::getUserId)
                .distinct()
                .map(internalUserDao::getById)
                .filter(user -> user != null)
                .map(this::buildBoundUser)
                .toList();
    }

    private OrgNodeBoundUserVO buildBoundUser(InternalUserEntity user) {
        OrgNodeBoundUserVO vo = new OrgNodeBoundUserVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setName(user.getName());
        vo.setDataScopeType(user.getDataScopeType());
        vo.setCampusId(user.getCampusId());
        vo.setOrgNodeId(user.getOrgNodeId());
        return vo;
    }
}
