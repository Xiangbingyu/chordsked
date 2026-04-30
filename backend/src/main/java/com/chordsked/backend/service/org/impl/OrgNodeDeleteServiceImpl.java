package com.chordsked.backend.service.org.impl;

import com.chordsked.backend.audit.annotation.AuditLog;
import com.chordsked.backend.dao.CampusDao;
import com.chordsked.backend.dao.OrgNodeDao;
import com.chordsked.backend.dao.TeacherUserDao;
import com.chordsked.backend.dao.UserCampusDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.entity.OrgNodeEntity;
import com.chordsked.backend.model.enums.OrgNodeType;
import com.chordsked.backend.service.org.OrgNodeDeleteService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("orgNodeDeleteService")
public class OrgNodeDeleteServiceImpl implements OrgNodeDeleteService {
    @Resource(name = "campusDao")
    private CampusDao campusDao;

    @Resource(name = "orgNodeDao")
    private OrgNodeDao orgNodeDao;

    @Resource(name = "userCampusDao")
    private UserCampusDao userCampusDao;

    @Resource(name = "teacherUserDao")
    private TeacherUserDao teacherUserDao;

    @Override
    @AuditLog(module = "ORG_MANAGEMENT", action = "DELETE_ORG_NODE")
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long nodeId) {
        if (nodeId == null || nodeId <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "节点ID无效");
        }
        OrgNodeEntity existing = orgNodeDao.getById(nodeId);
        if (existing == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "节点不存在");
        }
        if (orgNodeDao.countChildrenByParentId(nodeId) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "节点存在子节点，不能删除");
        }
        if (orgNodeDao.countUserBindingsByNodeId(nodeId) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "节点已绑定账号，不能删除");
        }
        if (existing.getNodeTypeEnum() == OrgNodeType.CAMPUS) {
            deleteCampusRoot(existing);
            return;
        }
        if (orgNodeDao.deleteById(nodeId) <= 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "删除组织节点失败");
        }
    }

    private void deleteCampusRoot(OrgNodeEntity existing) {
        Long campusId = existing.getCampusId();
        if (campusId == null || campusId <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "CAMPUS 节点缺少关联校区");
        }
        if (userCampusDao.countByCampusId(campusId) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "校区有教务账号绑定，请先移除用户");
        }
        if (teacherUserDao.countByCampusId(campusId) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "校区有教师绑定，请先移除教师");
        }
        if (orgNodeDao.deleteById(existing.getId()) <= 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "删除组织节点失败");
        }
        if (campusDao.deleteById(campusId) <= 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "删除关联校区失败");
        }
    }
}
