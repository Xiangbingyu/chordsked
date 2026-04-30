package com.chordsked.backend.service.org.impl;

import com.chordsked.backend.dao.OrgNodeDao;
import com.chordsked.backend.model.entity.OrgNodeEntity;
import com.chordsked.backend.model.vo.org.OrgNodeOptionVO;
import com.chordsked.backend.service.org.OrgNodeOptionQueryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("orgNodeOptionQueryService")
public class OrgNodeOptionQueryServiceImpl implements OrgNodeOptionQueryService {
    @Resource(name = "orgNodeDao")
    private OrgNodeDao orgNodeDao;

    @Override
    public List<OrgNodeOptionVO> list() {
        return orgNodeDao.listAllEnabled().stream()
                .map(this::buildOption)
                .toList();
    }

    private OrgNodeOptionVO buildOption(OrgNodeEntity node) {
        OrgNodeOptionVO option = new OrgNodeOptionVO();
        option.setId(node.getId());
        option.setParentId(node.getParentId());
        option.setNodeType(node.getNodeType());
        option.setName(node.getName());
        option.setStatus(node.getStatus());
        return option;
    }
}
