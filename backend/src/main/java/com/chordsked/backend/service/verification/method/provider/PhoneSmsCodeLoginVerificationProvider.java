package com.chordsked.backend.service.verification.method.provider;

import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.auth.AuthLoginMethod;
import com.chordsked.backend.model.dto.auth.AuthLoginRequest;
import com.chordsked.backend.model.vo.auth.AuthLoginResultVO;
import org.springframework.stereotype.Component;

/**
 * 手机号验证码登录校验 provider。
 * 当前仅负责识别并规范化手机号验证码登录请求，真实验证码校验逻辑后续可在此扩展。
 */
@Component("phoneSmsCodeLoginVerificationProvider")
public class PhoneSmsCodeLoginVerificationProvider implements LoginVerificationProvider {
    @Override
    public AuthLoginMethod getLoginMethod() {
        return AuthLoginMethod.PHONE_SMS_CODE;
    }

    @Override
    /**
     * 当前阶段先完成参数校验与 principal 规范化。
     * 待手机号验证码登录正式开放后，可在此接入验证码校验、装载快照与登录结果组装。
     */
    public AuthLoginResultVO verify(AuthLoginRequest request) {
        if (request == null || request.getPhone() == null || request.getSmsCode() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "登录请求参数不合法");
        }
        String principal = request.getPhone().trim();
        if (principal.isEmpty() || request.getSmsCode().isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "登录请求参数不合法");
        }
        request.setPhone(principal);
        throw new BusinessException(ErrorCode.BAD_REQUEST, "手机号验证码登录暂未开放");
    }
}
