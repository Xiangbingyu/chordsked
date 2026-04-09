package com.chordsked.backend.service.verification.method.provider;

import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.auth.AuthLoginMethod;
import com.chordsked.backend.model.dto.auth.AuthLoginRequest;
import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.model.vo.auth.AuthLoginResultVO;
import org.springframework.stereotype.Component;

/**
 * 账号密码加短信验证码登录 provider。
 * 当前保留参数校验与 principal 规范化逻辑，后续可在此接入双因子校验与快照装载流程。
 */
@Component("usernamePasswordSmsCodeLoginVerificationProvider")
public class UsernamePasswordSmsCodeLoginVerificationProvider implements LoginVerificationProvider {
    @Override
    public AuthLoginMethod getLoginMethod() {
        return AuthLoginMethod.USERNAME_PASSWORD_SMS_CODE;
    }

    @Override
    /**
     * 当前阶段尚未开放该登录方式，因此仅保留基础入参校验。
     * 后续扩展时可直接在此补充密码校验、短信验证码校验与 support 装载调用。
     */
    public AuthLoginResultVO verify(AuthLoginRequest request, AccountUserType userType) {
        if (request == null || request.getUsername() == null
                || request.getPassword() == null || request.getSmsCode() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "登录请求参数不合法");
        }
        String principal = request.getUsername().trim();
        if (principal.isEmpty() || request.getPassword().isBlank() || request.getSmsCode().isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "登录请求参数不合法");
        }
        request.setUsername(principal);
        throw new BusinessException(ErrorCode.BAD_REQUEST, "账号密码验证码登录暂未开放");
    }
}
