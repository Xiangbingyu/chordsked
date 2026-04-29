package com.chordsked.backend.service.campus.impl;

import com.chordsked.backend.dao.CampusDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.enums.CampusStatus;
import com.chordsked.backend.service.campus.CampusWriteValidator;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service("campusWriteValidator")
public class CampusWriteValidatorImpl implements CampusWriteValidator {
    @Resource(name = "campusDao")
    private CampusDao campusDao;

    @Override
    public String validateAndNormalizeCode(String code) {
        String normalizedCode = code == null ? null : code.trim();
        if (normalizedCode == null || normalizedCode.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "校区编码不能为空");
        }
        if (normalizedCode.length() > 50) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "校区编码长度不能超过50");
        }
        return normalizedCode;
    }

    @Override
    public String validateAndNormalizeName(String name) {
        String normalizedName = name == null ? null : name.trim();
        if (normalizedName == null || normalizedName.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "校区名称不能为空");
        }
        if (normalizedName.length() > 100) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "校区名称长度不能超过100");
        }
        return normalizedName;
    }

    @Override
    public Integer validateStatus(Integer status) {
        if (CampusStatus.fromCode(status) == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "状态值无效");
        }
        return status;
    }

    @Override
    public void validateCodeForCreate(String code) {
        if (campusDao.existsByCode(code)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "校区编码已存在，请更换");
        }
    }

    @Override
    public void validateNameForCreate(String name) {
        if (campusDao.existsByName(name)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "校区名称已存在，请更换");
        }
    }

    @Override
    public void validateCodeForUpdate(String code, Long campusId) {
        if (campusDao.existsByCodeExcludeId(code, campusId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "校区编码已存在，请更换");
        }
    }

    @Override
    public void validateNameForUpdate(String name, Long campusId) {
        if (campusDao.existsByNameExcludeId(name, campusId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "校区名称已存在，请更换");
        }
    }
}
