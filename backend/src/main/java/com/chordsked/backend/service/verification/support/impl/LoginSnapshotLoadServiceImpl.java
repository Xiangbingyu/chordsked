package com.chordsked.backend.service.verification.support.impl;

import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.auth.AuthLoginSnapshot;
import com.chordsked.backend.model.auth.AuthLoginMethod;
import com.chordsked.backend.model.dto.auth.AuthLoginRequest;
import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.service.verification.support.LoginSnapshotLoadService;
import com.chordsked.backend.service.verification.support.provider.LoginSnapshotLoadProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录快照装载路由服务。
 * 负责在 support 模块内按 loginMethod 与 userType 双重分发到对应装载 provider，
 * 使登录方式 provider 不需要直接感知不同快照的来源细节。
 */
@Service("loginSnapshotLoadService")
public class LoginSnapshotLoadServiceImpl implements LoginSnapshotLoadService {
    private final Map<LoginSnapshotRouteKey, LoginSnapshotLoadProvider> providerRoute;

    /**
     * 构建“登录方式 + 用户类型 -> 快照装载 provider”映射。
     * 后续新增新的登录方式快照装载时，只需补充 provider 实现并声明对应的 loginMethod 与 userType。
     */
    public LoginSnapshotLoadServiceImpl(List<LoginSnapshotLoadProvider> providers) {
        Map<LoginSnapshotRouteKey, LoginSnapshotLoadProvider> route = new ConcurrentHashMap<>();
        for (LoginSnapshotLoadProvider provider : providers) {
            LoginSnapshotRouteKey routeKey = new LoginSnapshotRouteKey(provider.getLoginMethod(), provider.getUserType());
            LoginSnapshotLoadProvider previousProvider = route.put(routeKey, provider);
            if (previousProvider != null) {
                throw new IllegalStateException(
                        "Duplicate login snapshot load provider for method: "
                                + provider.getLoginMethod()
                                + ", userType: "
                                + provider.getUserType()
                );
            }
        }
        this.providerRoute = route;
    }

    @Override
    /**
     * 按登录方式装载快照，并校验返回类型是否与调用方预期一致。
     * 此层只做路由与类型守卫，具体缓存回源策略由各 provider 自行实现。
     */
    public <T extends AuthLoginSnapshot> T load(AuthLoginRequest request, String principal, Class<T> snapshotType) {
        if (request == null || request.getLoginMethod() == null || request.getUserType() == null
                || principal == null || principal.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "登录快照装载参数不合法");
        }
        LoginSnapshotLoadProvider provider = providerRoute.get(
                new LoginSnapshotRouteKey(request.getLoginMethod(), request.getUserType())
        );
        if (provider == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "当前账号类型暂不支持该登录方式");
        }
        AuthLoginSnapshot snapshot = provider.load(request, principal);
        if (snapshot == null) {
            return null;
        }
        if (!snapshotType.isInstance(snapshot)) {
            throw new IllegalStateException("Unexpected login snapshot type: " + snapshot.getClass().getName());
        }
        return snapshotType.cast(snapshot);
    }

    private record LoginSnapshotRouteKey(AuthLoginMethod loginMethod, AccountUserType userType) {
    }
}
