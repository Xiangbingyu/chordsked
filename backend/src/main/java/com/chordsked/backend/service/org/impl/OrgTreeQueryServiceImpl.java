package com.chordsked.backend.service.org.impl;

import com.chordsked.backend.dao.OrgNodeDao;
import com.chordsked.backend.model.entity.OrgNodeEntity;
import com.chordsked.backend.model.vo.org.OrgTreeNodeVO;
import com.chordsked.backend.service.org.OrgTreeQueryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service("orgTreeQueryService")
public class OrgTreeQueryServiceImpl implements OrgTreeQueryService {
    @Resource(name = "orgNodeDao")
    private OrgNodeDao orgNodeDao;

    @Override
    public List<OrgTreeNodeVO> listTree() {
        List<OrgNodeEntity> nodes = orgNodeDao.listAllEnabled();
        if (nodes.isEmpty()) {
            return List.of();
        }
        Map<Long, OrgTreeNodeVO> nodeMap = new LinkedHashMap<>();
        for (OrgNodeEntity node : nodes) {
            OrgTreeNodeVO vo = new OrgTreeNodeVO();
            vo.setId(node.getId());
            vo.setParentId(node.getParentId());
            vo.setNodeType(node.getNodeType());
            vo.setCode(node.getCode());
            vo.setName(node.getName());
            vo.setCampusId(node.getCampusId());
            vo.setLevel(node.getLevel());
            vo.setSort(node.getSort());
            vo.setStatus(node.getStatus());
            vo.setRemark(node.getRemark());
            vo.setBoundUserCount(orgNodeDao.countUserBindingsByNodeId(node.getId()));
            vo.setHasChildren(false);
            vo.setChildren(new ArrayList<>());
            nodeMap.put(node.getId(), vo);
        }
        List<OrgTreeNodeVO> roots = new ArrayList<>();
        for (OrgTreeNodeVO node : nodeMap.values()) {
            if (node.getParentId() == null || node.getParentId() <= 0) {
                roots.add(node);
                continue;
            }
            OrgTreeNodeVO parent = nodeMap.get(node.getParentId());
            if (parent == null) {
                roots.add(node);
                continue;
            }
            parent.getChildren().add(node);
            parent.setHasChildren(true);
        }
        return roots;
    }
}
