package com.chordsked.backend.service.campus.impl;

import com.chordsked.backend.dao.CampusDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.vo.campus.CampusDetailResultVO;
import com.chordsked.backend.service.campus.CampusOperationGuardService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service("campusOperationGuardService")
public class CampusOperationGuardServiceImpl implements CampusOperationGuardService {
    @Resource(name = "campusDao")
    private CampusDao campusDao;

    @Override
    public CampusDetailResultVO validateOperationTarget(Long campusId, String actionName) {
        if (campusId == null || campusId <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "campusId必须大于0");
        }
        CampusDetailResultVO campus = campusDao.getDetailById(campusId);
        if (campus == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "校区不存在");
        }
        return campus;
    }
}
