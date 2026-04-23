package com.chordsked.backend.service.internaluser.impl;

import com.chordsked.backend.dao.InternalUserDao;
import com.chordsked.backend.dao.UserCampusDao;
import com.chordsked.backend.dao.UserRoleDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.dto.internaluser.InternalUserDetailQueryRequest;
import com.chordsked.backend.model.entity.UserRoleEntity;
import com.chordsked.backend.model.vo.internaluser.InternalUserDetailResultVO;
import com.chordsked.backend.service.internaluser.InternalUserDetailQueryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("internalUserDetailQueryService")
public class InternalUserDetailQueryServiceImpl implements InternalUserDetailQueryService {
    @Resource(name = "internalUserDao")
    private InternalUserDao internalUserDao;

    @Resource(name = "userRoleDao")
    private UserRoleDao userRoleDao;

    @Resource(name = "userCampusDao")
    private UserCampusDao userCampusDao;

    @Override
    public InternalUserDetailResultVO getDetail(InternalUserDetailQueryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        Long userId = request.getUserId();
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "userId必须大于0");
        }
        InternalUserDetailResultVO detail = internalUserDao.getAccessibleDetailById(userId);
        if (detail == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限查看该账号");
        }
        List<UserRoleEntity> userRoles = userRoleDao.listByUserIds(List.of(userId));
        detail.setRoleIds(userRoles.stream().map(UserRoleEntity::getRoleId).toList());
        detail.setCampusIds(userCampusDao.listCampusIdsByUserId(userId));
        detail.setPrimaryCampusId(userCampusDao.getPrimaryCampusIdByUserId(userId));
        return detail;
    }
}
