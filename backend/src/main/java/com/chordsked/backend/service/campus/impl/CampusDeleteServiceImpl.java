package com.chordsked.backend.service.campus.impl;

import com.chordsked.backend.audit.annotation.AuditLog;
import com.chordsked.backend.dao.CampusDao;
import com.chordsked.backend.dao.TeacherUserDao;
import com.chordsked.backend.dao.UserCampusDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.dto.campus.CampusDeleteRequest;
import com.chordsked.backend.model.vo.campus.CampusDetailResultVO;
import com.chordsked.backend.service.campus.CampusDeleteService;
import com.chordsked.backend.service.campus.CampusOperationGuardService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("campusDeleteService")
public class CampusDeleteServiceImpl implements CampusDeleteService {
    @Resource(name = "campusDao")
    private CampusDao campusDao;

    @Resource(name = "userCampusDao")
    private UserCampusDao userCampusDao;

    @Resource(name = "teacherUserDao")
    private TeacherUserDao teacherUserDao;

    @Resource(name = "campusOperationGuardService")
    private CampusOperationGuardService campusOperationGuardService;

    @Override
    @AuditLog(module = "CAMPUS_MANAGEMENT", action = "DELETE_CAMPUS")
    @Transactional(rollbackFor = Exception.class)
    public void delete(CampusDeleteRequest request) {
        if (request == null || request.getCampusId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请求参数不能为空");
        }
        Long campusId = request.getCampusId();
        CampusDetailResultVO campus = campusOperationGuardService.validateOperationTarget(campusId, "删除");

        int internalUserCount = userCampusDao.countByCampusId(campusId);
        if (internalUserCount > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "校区有教务账号绑定，请先移除用户");
        }

        int teacherCount = teacherUserDao.countByCampusId(campusId);
        if (teacherCount > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "校区有教师绑定，请先移除教师");
        }

        int rows = campusDao.deleteById(campusId);
        if (rows == 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "校区不存在");
        }
    }
}
