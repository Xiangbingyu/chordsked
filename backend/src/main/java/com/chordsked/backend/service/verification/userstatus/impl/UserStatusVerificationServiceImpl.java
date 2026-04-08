package com.chordsked.backend.service.verification.userstatus.impl;

import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.service.verification.userstatus.UserStatusVerificationService;
import com.chordsked.backend.service.verification.userstatus.provider.UserStatusVerificationProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户状态校验服务实现。
 * 启动时收集全部 provider，并按 userType 建立路由，供 refresh 等鉴权链路复用。
 */
@Service("userStatusVerificationService")
public class UserStatusVerificationServiceImpl implements UserStatusVerificationService {
    private final Map<AccountUserType, UserStatusVerificationProvider> providerRoute;

    /**
     * 构建 userType 到 provider 的路由表。
     * 若出现重复 provider，则在启动期直接失败，避免运行期路由歧义。
     */
    public UserStatusVerificationServiceImpl(List<UserStatusVerificationProvider> providers) {
        Map<AccountUserType, UserStatusVerificationProvider> route = new ConcurrentHashMap<>();
        for (UserStatusVerificationProvider provider : providers) {
            UserStatusVerificationProvider previousProvider = route.put(provider.getUserType(), provider);
            if (previousProvider != null) {
                throw new IllegalStateException("Duplicate user status verification provider for user type: " + provider.getUserType());
            }
        }
        this.providerRoute = route;
    }

    @Override
    public void verify(Long userId, AccountUserType userType) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录已过期，请重新登录");
        }
        if (userType == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录已过期，请重新登录");
        }
        UserStatusVerificationProvider provider = providerRoute.get(userType);
        if (provider == null) {
            throw new IllegalStateException("User status verification provider not found for user type: " + userType);
        }
        provider.verify(userId);
    }
}
