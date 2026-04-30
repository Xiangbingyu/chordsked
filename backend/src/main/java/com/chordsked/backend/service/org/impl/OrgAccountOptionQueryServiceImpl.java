package com.chordsked.backend.service.org.impl;

import com.chordsked.backend.dao.InternalUserDao;
import com.chordsked.backend.model.dto.internaluser.InternalUserQueryRequest;
import com.chordsked.backend.model.enums.UserDataScopeType;
import com.chordsked.backend.model.vo.internaluser.InternalUserQueryResultVO;
import com.chordsked.backend.model.vo.org.OrgAccountOptionVO;
import com.chordsked.backend.service.org.OrgAccountOptionQueryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("orgAccountOptionQueryService")
public class OrgAccountOptionQueryServiceImpl implements OrgAccountOptionQueryService {
    @Resource(name = "internalUserDao")
    private InternalUserDao internalUserDao;

    @Override
    public List<OrgAccountOptionVO> list() {
        InternalUserQueryRequest request = new InternalUserQueryRequest();
        request.setPage(1);
        request.setPageSize(200);
        List<InternalUserQueryResultVO> users = internalUserDao.listByQuery(request);
        return users.stream()
                .filter(user -> user.getDataScopeTypeEnum() == UserDataScopeType.ASSIGNED)
                .filter(user -> user.getOrgNodeId() != null && user.getOrgNodeId() > 0)
                .map(this::buildOption)
                .toList();
    }

    private OrgAccountOptionVO buildOption(InternalUserQueryResultVO user) {
        OrgAccountOptionVO option = new OrgAccountOptionVO();
        option.setUserId(user.getId());
        option.setUsername(user.getUsername());
        option.setName(user.getName());
        option.setDataScopeType(user.getDataScopeType());
        option.setCampusId(user.getCampusId());
        option.setOrgNodeId(user.getOrgNodeId());
        return option;
    }
}
