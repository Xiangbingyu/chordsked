package com.chordsked.backend.service.org.impl;

import com.chordsked.backend.audit.annotation.AuditLog;
import com.chordsked.backend.dao.CampusDao;
import com.chordsked.backend.dao.OrgNodeDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.dto.org.OrgNodeUpdateRequest;
import com.chordsked.backend.model.entity.CampusEntity;
import com.chordsked.backend.model.entity.OrgNodeEntity;
import com.chordsked.backend.model.enums.CampusStatus;
import com.chordsked.backend.model.enums.OrgNodeStatus;
import com.chordsked.backend.model.enums.OrgNodeType;
import com.chordsked.backend.service.org.OrgNodeUpdateService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("orgNodeUpdateService")
public class OrgNodeUpdateServiceImpl implements OrgNodeUpdateService {
    @Resource(name = "campusDao")
    private CampusDao campusDao;

    @Resource(name = "orgNodeDao")
    private OrgNodeDao orgNodeDao;

    @Override
    @AuditLog(module = "ORG_MANAGEMENT", action = "UPDATE_ORG_NODE")
    @Transactional(rollbackFor = Exception.class)
    public void update(OrgNodeUpdateRequest request) {
        if (request == null || request.getNodeId() == null || request.getNodeId() <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "节点ID无效");
        }
        OrgNodeEntity existing = orgNodeDao.getById(request.getNodeId());
        if (existing == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "节点不存在");
        }
        String code = request.getCode() == null ? null : request.getCode().trim();
        String name = request.getName() == null ? null : request.getName().trim();
        if (code == null || code.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "节点编码不能为空");
        }
        if (name == null || name.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "节点名称不能为空");
        }
        if (orgNodeDao.existsByParentIdAndCode(existing.getParentId(), code, request.getNodeId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "同级节点编码已存在");
        }
        if (orgNodeDao.existsByParentIdAndName(existing.getParentId(), name, request.getNodeId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "同级节点名称已存在");
        }
        OrgNodeStatus status = OrgNodeStatus.fromCode(request.getStatus());
        if (status == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "节点状态无效");
        }
        if (existing.getNodeTypeEnum() == OrgNodeType.CAMPUS) {
            syncCampus(existing, code, name, request.getSort(), status, request.getRemark());
        }
        OrgNodeEntity entity = new OrgNodeEntity();
        entity.setId(request.getNodeId());
        entity.setCode(code);
        entity.setName(name);
        entity.setSort(request.getSort() == null ? 0 : request.getSort());
        entity.setStatus(status.getCode());
        entity.setRemark(request.getRemark());
        entity.setUpdatedAt(System.currentTimeMillis());
        int affectedRows = orgNodeDao.updateById(entity);
        if (affectedRows <= 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "更新组织节点失败");
        }
    }

    private void syncCampus(OrgNodeEntity existing, String code, String name, Integer sort, OrgNodeStatus status, String remark) {
        Long campusId = existing.getCampusId();
        if (campusId == null || campusId <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "CAMPUS 节点缺少关联校区");
        }
        if (campusDao.existsByCodeExcludeId(code, campusId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "同级节点编码已存在");
        }
        if (campusDao.existsByNameExcludeId(name, campusId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "同级节点名称已存在");
        }
        CampusEntity campus = new CampusEntity();
        campus.setId(campusId);
        campus.setCode(code);
        campus.setName(name);
        campus.setSort(sort == null ? 0 : sort);
        campus.setStatus(status == OrgNodeStatus.ENABLED ? CampusStatus.ENABLED.getCode() : CampusStatus.DISABLED.getCode());
        campus.setRemark(remark);
        campus.setUpdatedAt(System.currentTimeMillis());
        if (campusDao.updateById(campus) <= 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "更新关联校区失败");
        }
    }
}
