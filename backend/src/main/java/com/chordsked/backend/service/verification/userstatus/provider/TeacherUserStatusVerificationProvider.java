package com.chordsked.backend.service.verification.userstatus.provider;

import com.chordsked.backend.dao.TeacherUserDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.entity.TeacherUserEntity;
import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.model.enums.TeacherUserStatus;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 教师账号状态校验 provider。
 */
@Component("teacherUserStatusVerificationProvider")
public class TeacherUserStatusVerificationProvider implements UserStatusVerificationProvider {
    @Resource(name = "teacherUserDao")
    private TeacherUserDao teacherUserDao;

    @Override
    public AccountUserType getUserType() {
        return AccountUserType.TEACHER;
    }

    @Override
    public void verify(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录已过期，请重新登录");
        }
        TeacherUserEntity teacherUser = teacherUserDao.getById(userId);
        if (teacherUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录已过期，请重新登录");
        }
        if (!TeacherUserStatus.ON_DUTY.equals(teacherUser.getStatusEnum())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "账号已被禁用，请联系管理员");
        }
    }
}
