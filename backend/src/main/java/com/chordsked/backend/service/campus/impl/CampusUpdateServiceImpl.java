package com.chordsked.backend.service.campus.impl;

import com.chordsked.backend.audit.annotation.AuditLog;
import com.chordsked.backend.dao.CampusDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.dto.campus.CampusUpdateRequest;
import com.chordsked.backend.model.entity.CampusEntity;
import com.chordsked.backend.model.vo.campus.CampusDetailResultVO;
import com.chordsked.backend.service.campus.CampusOperationGuardService;
import com.chordsked.backend.service.campus.CampusUpdateService;
import com.chordsked.backend.service.campus.CampusWriteValidator;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("campusUpdateService")
public class CampusUpdateServiceImpl implements CampusUpdateService {
    @Resource(name = "campusDao")
    private CampusDao campusDao;

    @Resource(name = "campusOperationGuardService")
    private CampusOperationGuardService campusOperationGuardService;

    @Resource(name = "campusWriteValidator")
    private CampusWriteValidator campusWriteValidator;

    @Override
    @AuditLog(module = "CAMPUS_MANAGEMENT", action = "UPDATE_CAMPUS")
    @Transactional(rollbackFor = Exception.class)
    public void update(CampusUpdateRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请求参数不能为空");
        }
        Long campusId = request.getCampusId();
        CampusDetailResultVO existingCampus = campusOperationGuardService.validateOperationTarget(campusId, "修改");

        String code = campusWriteValidator.validateAndNormalizeCode(request.getCode());
        String name = campusWriteValidator.validateAndNormalizeName(request.getName());
        Integer status = campusWriteValidator.validateStatus(request.getStatus());
        campusWriteValidator.validateCodeForUpdate(code, campusId);
        campusWriteValidator.validateNameForUpdate(name, campusId);

        long now = System.currentTimeMillis();
        CampusEntity campus = new CampusEntity();
        campus.setId(campusId);
        campus.setCode(code);
        campus.setName(name);
        campus.setAddress(request.getAddress());
        campus.setPhone(request.getPhone());
        campus.setSort(request.getSort() == null ? 0 : request.getSort());
        campus.setStatus(status);
        campus.setRemark(request.getRemark());
        campus.setUpdatedAt(now);
        campusDao.updateById(campus);
    }
}
