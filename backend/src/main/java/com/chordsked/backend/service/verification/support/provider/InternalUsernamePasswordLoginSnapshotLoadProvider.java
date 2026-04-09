package com.chordsked.backend.service.verification.support.provider;

import com.chordsked.backend.cache.auth.provider.AuthLoginSnapshotCacheProvider;
import com.chordsked.backend.dao.InternalUserDao;
import com.chordsked.backend.model.auth.AuthLoginSnapshot;
import com.chordsked.backend.model.auth.AuthLoginMethod;
import com.chordsked.backend.model.auth.UsernamePasswordLoginSnapshot;
import com.chordsked.backend.model.dto.auth.AuthLoginRequest;
import com.chordsked.backend.model.entity.InternalUserEntity;
import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.model.enums.InternalUserStatus;
import com.chordsked.backend.model.enums.MustChangePasswordFlag;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 内部账号的用户名密码登录快照装载 provider。
 * 负责把内部账号数据转换为 UsernamePasswordLoginSnapshot，
 * 并在缓存可用时优先使用快照缓存，减少重复回源查询。
 */
@Component("internalUsernamePasswordLoginSnapshotLoadProvider")
public class InternalUsernamePasswordLoginSnapshotLoadProvider implements LoginSnapshotLoadProvider {
    @Resource(name = "usernamePasswordLoginSnapshotCacheProvider")
    private AuthLoginSnapshotCacheProvider usernamePasswordLoginSnapshotCacheProvider;

    @Resource(name = "internalUserDao")
    private InternalUserDao internalUserDao;

    @Override
    public AuthLoginMethod getLoginMethod() {
        return AuthLoginMethod.USERNAME_PASSWORD;
    }

    @Override
    public AccountUserType getUserType() {
        return AccountUserType.ADMIN;
    }

    @Override
    /**
     * 先尝试从登录快照缓存装载，缓存未命中时回源 InternalUserDao，
     * 再将结果组装为用户名密码登录快照并写回缓存。
     * 当前实现面向内部账号，后续若其他账号来源也支持该登录方式，可新增新的 provider。
     */
    public AuthLoginSnapshot load(AuthLoginRequest request, AccountUserType userType, String principal) {
        UsernamePasswordLoginSnapshot cachedSnapshot = getCachedSnapshot(userType, principal);
        if (cachedSnapshot != null) {
            return cachedSnapshot;
        }
        InternalUserEntity internalUser = internalUserDao.getByUsername(principal);
        if (internalUser == null) {
            return null;
        }
        UsernamePasswordLoginSnapshot snapshot = new UsernamePasswordLoginSnapshot();
        snapshot.setUserId(internalUser.getId());
        snapshot.setUserType(userType);
        snapshot.setLoginMethod(AuthLoginMethod.USERNAME_PASSWORD);
        snapshot.setPrincipal(internalUser.getUsername());
        snapshot.setName(internalUser.getName());
        snapshot.setEnabled(InternalUserStatus.ENABLED.equals(internalUser.getStatusEnum()));
        snapshot.setMustChangePassword(MustChangePasswordFlag.YES.equals(internalUser.getMustChangePasswordEnum()));
        snapshot.setPassword(internalUser.getPassword());
        snapshot.setPhone(internalUser.getPhone());
        usernamePasswordLoginSnapshotCacheProvider.cacheLoginSnapshot(snapshot);
        return snapshot;
    }

    /**
     * 从缓存中读取用户名密码登录快照。
     * 当前缓存 key 仍由 userType 和 principal 共同决定，便于兼容同一登录方式下的多账号来源。
     */
    private UsernamePasswordLoginSnapshot getCachedSnapshot(AccountUserType userType, String principal) {
        Object cachedSnapshot = usernamePasswordLoginSnapshotCacheProvider.getLoginSnapshot(userType, principal);
        if (cachedSnapshot instanceof UsernamePasswordLoginSnapshot usernamePasswordLoginSnapshot) {
            return usernamePasswordLoginSnapshot;
        }
        return null;
    }
}
