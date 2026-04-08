package com.chordsked.backend.service.verification.userstatus.provider;

import com.chordsked.backend.dao.StudentUserDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.entity.StudentUserEntity;
import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.model.enums.StudentUserStatus;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 学生账号状态校验 provider。
 */
@Component("studentUserStatusVerificationProvider")
public class StudentUserStatusVerificationProvider implements UserStatusVerificationProvider {
    @Resource(name = "studentUserDao")
    private StudentUserDao studentUserDao;

    @Override
    public AccountUserType getUserType() {
        return AccountUserType.STUDENT;
    }

    @Override
    public void verify(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录已过期，请重新登录");
        }
        StudentUserEntity studentUser = studentUserDao.getById(userId);
        if (studentUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录已过期，请重新登录");
        }
        if (!StudentUserStatus.ENABLED.equals(studentUser.getStatusEnum())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "账号已被禁用，请联系管理员");
        }
    }
}
