package com.chordsked.backend.service.campus.impl;

import com.chordsked.backend.audit.annotation.AuditLog;
import com.chordsked.backend.dao.CampusDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.dto.campus.CampusCreateRequest;
import com.chordsked.backend.model.entity.CampusEntity;
import com.chordsked.backend.service.campus.CampusCreateService;
import com.chordsked.backend.service.campus.CampusWriteValidator;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("campusCreateService")
public class CampusCreateServiceImpl implements CampusCreateService {
    @Resource(name = "campusDao")
    private CampusDao campusDao;

    @Resource(name = "campusWriteValidator")
    private CampusWriteValidator campusWriteValidator;

    @Override
    @AuditLog(module = "CAMPUS_MANAGEMENT", action = "CREATE_CAMPUS")
    @Transactional(rollbackFor = Exception.class)
    public Long create(CampusCreateRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请求参数不能为空");
        }
        String code = campusWriteValidator.validateAndNormalizeCode(request.getCode());
        String name = campusWriteValidator.validateAndNormalizeName(request.getName());
        Integer status = campusWriteValidator.validateStatus(request.getStatus());
        campusWriteValidator.validateCodeForCreate(code);
        campusWriteValidator.validateNameForCreate(name);

        long now = System.currentTimeMillis();
        CampusEntity campus = new CampusEntity();
        campus.setCode(code);
        campus.setName(name);
        campus.setAddress(request.getAddress());
        campus.setPhone(request.getPhone());
        campus.setSort(request.getSort() == null ? 0 : request.getSort());
        campus.setStatus(status);
        campus.setRemark(request.getRemark());
        campus.setCreatedAt(now);
        campus.setUpdatedAt(now);
        campusDao.insert(campus);
        return campus.getId();
    }
}
