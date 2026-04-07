package com.chordsked.backend.service.verification.method.impl;

import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.auth.AuthLoginMethod;
import com.chordsked.backend.model.dto.auth.AuthLoginRequest;
import com.chordsked.backend.model.vo.auth.AuthLoginResultVO;
import com.chordsked.backend.service.verification.method.LoginMethodVerificationService;
import com.chordsked.backend.service.verification.method.provider.LoginVerificationProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录方式验证路由服务。
 * 负责在启动阶段收集所有登录方式 provider，并在运行时按 loginMethod 分发请求。
 * 后续若新增新的登录方式，只需要补充对应 provider，无需修改此路由实现。
 */
@Service("loginMethodVerificationService")
public class LoginMethodVerificationServiceImpl implements LoginMethodVerificationService {
    private final Map<AuthLoginMethod, LoginVerificationProvider> providerRoute;

    /**
     * 构建“登录方式 -> 登录校验 provider”路由表。
     * 同一登录方式若出现多个 provider，会在启动阶段直接失败，避免运行期歧义。
     */
    public LoginMethodVerificationServiceImpl(List<LoginVerificationProvider> providers) {
        Map<AuthLoginMethod, LoginVerificationProvider> route = new ConcurrentHashMap<>();
        for (LoginVerificationProvider provider : providers) {
            LoginVerificationProvider previousProvider = route.put(provider.getLoginMethod(), provider);
            if (previousProvider != null) {
                throw new IllegalStateException("Duplicate login verification provider for method: " + provider.getLoginMethod());
            }
        }
        this.providerRoute = route;
    }

    @Override
    /**
     * 按请求中声明的登录方式执行校验。
     * 此层不关心不同账号类型的数据来源，只负责把请求交给对应登录方式 provider。
     */
    public AuthLoginResultVO verify(AuthLoginRequest request) {
        if (request == null || request.getLoginMethod() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "登录请求参数不合法");
        }
        LoginVerificationProvider provider = providerRoute.get(request.getLoginMethod());
        if (provider == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "当前登录方式暂不支持");
        }
        return provider.verify(request);
    }
}
