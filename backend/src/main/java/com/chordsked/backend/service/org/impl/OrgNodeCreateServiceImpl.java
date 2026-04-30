package com.chordsked.backend.service.org.impl;

import com.chordsked.backend.audit.annotation.AuditLog;
import com.chordsked.backend.dao.CampusDao;
import com.chordsked.backend.dao.OrgNodeDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.dto.org.OrgNodeCreateRequest;
import com.chordsked.backend.model.entity.CampusEntity;
import com.chordsked.backend.model.entity.OrgNodeEntity;
import com.chordsked.backend.model.enums.CampusStatus;
import com.chordsked.backend.model.enums.OrgNodeStatus;
import com.chordsked.backend.model.enums.OrgNodeType;
import com.chordsked.backend.service.org.OrgNodeCreateService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("orgNodeCreateService")
public class OrgNodeCreateServiceImpl implements OrgNodeCreateService {
    @Resource(name = "campusDao")
    private CampusDao campusDao;

    @Resource(name = "orgNodeDao")
    private OrgNodeDao orgNodeDao;

    @Override
    @AuditLog(module = "ORG_MANAGEMENT", action = "CREATE_ORG_NODE")
    @Transactional(rollbackFor = Exception.class)
    public Long create(OrgNodeCreateRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请求参数不能为空");
        }
        OrgNodeType nodeType = OrgNodeType.fromCode(request.getNodeType());
        if (nodeType == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "节点类型无效");
        }
        String code = request.getCode() == null ? null : request.getCode().trim();
        String name = request.getName() == null ? null : request.getName().trim();
        if (code == null || code.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "节点编码不能为空");
        }
        if (name == null || name.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "节点名称不能为空");
        }
        OrgNodeStatus status = OrgNodeStatus.fromCode(request.getStatus());
        if (status == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "节点状态无效");
        }

        long now = System.currentTimeMillis();
        if (nodeType.isCampus()) {
            validateCampusRootCreate(request.getParentId(), code, name);
            CampusEntity campus = buildCampusEntity(code, name, request.getSort(), status, request.getRemark(), now);
            campusDao.insert(campus);

            OrgNodeEntity entity = new OrgNodeEntity();
            entity.setParentId(0L);
            entity.setNodeType(nodeType.getCode());
            entity.setCode(code);
            entity.setName(name);
            entity.setCampusId(campus.getId());
            entity.setAncestors("");
            entity.setLevel(1);
            entity.setSort(request.getSort() == null ? 0 : request.getSort());
            entity.setStatus(status.getCode());
            entity.setRemark(request.getRemark());
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            orgNodeDao.save(entity);
            return entity.getId();
        }

        OrgNodeEntity parent = orgNodeDao.getById(request.getParentId());
        if (parent == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "父节点不存在");
        }
        validateParent(nodeType, parent);
        if (orgNodeDao.existsByParentIdAndCode(parent.getId(), code, null)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "同级节点编码已存在");
        }
        if (orgNodeDao.existsByParentIdAndName(parent.getId(), name, null)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "同级节点名称已存在");
        }

        OrgNodeEntity entity = new OrgNodeEntity();
        entity.setParentId(parent.getId());
        entity.setNodeType(nodeType.getCode());
        entity.setCode(code);
        entity.setName(name);
        entity.setCampusId(parent.getCampusId());
        entity.setAncestors(buildAncestors(parent));
        entity.setLevel((parent.getLevel() == null ? 1 : parent.getLevel()) + 1);
        entity.setSort(request.getSort() == null ? 0 : request.getSort());
        entity.setStatus(status.getCode());
        entity.setRemark(request.getRemark());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        orgNodeDao.save(entity);
        return entity.getId();
    }

    private void validateCampusRootCreate(Long parentId, String code, String name) {
        if (parentId == null || parentId != 0L) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "CAMPUS 节点父级必须是根节点");
        }
        if (orgNodeDao.existsByParentIdAndCode(0L, code, null) || campusDao.existsByCode(code)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "同级节点编码已存在");
        }
        if (orgNodeDao.existsByParentIdAndName(0L, name, null) || campusDao.existsByName(name)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "同级节点名称已存在");
        }
    }

    private void validateParent(OrgNodeType nodeType, OrgNodeEntity parent) {
        OrgNodeType parentType = parent.getNodeTypeEnum();
        if (nodeType.isDept() && parentType != OrgNodeType.CAMPUS) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "DEPT 节点父级必须是 CAMPUS");
        }
        if (nodeType.isGroup() && parentType != OrgNodeType.DEPT) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "GROUP 节点父级必须是 DEPT");
        }
        if (parent.getStatusEnum() != OrgNodeStatus.ENABLED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "父节点未启用");
        }
    }

    private CampusEntity buildCampusEntity(String code, String name, Integer sort, OrgNodeStatus status, String remark, long now) {
        CampusEntity campus = new CampusEntity();
        campus.setCode(code);
        campus.setName(name);
        campus.setSort(sort == null ? 0 : sort);
        campus.setStatus(status == OrgNodeStatus.ENABLED ? CampusStatus.ENABLED.getCode() : CampusStatus.DISABLED.getCode());
        campus.setRemark(remark);
        campus.setCreatedAt(now);
        campus.setUpdatedAt(now);
        return campus;
    }

    private String buildAncestors(OrgNodeEntity parent) {
        if (parent.getAncestors() == null || parent.getAncestors().isBlank()) {
            return String.valueOf(parent.getId());
        }
        return parent.getAncestors() + "," + parent.getId();
    }
}
