package com.chordsked.backend.security.account.provider;

import com.chordsked.backend.cache.security.SecurityCacheService;
import com.chordsked.backend.dao.InternalUserDao;
import com.chordsked.backend.dao.UserCampusDao;
import com.chordsked.backend.model.entity.InternalUserEntity;
import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.model.enums.InternalUserStatus;
import com.chordsked.backend.model.permission.PermissionCodeResolver;
import com.chordsked.backend.security.account.model.ChordSkedUserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 教务端账号 Provider。
 * 负责根据 JWT 中的 userId 装载教务端 UserDetails，并与权限缓存、用户快照缓存协同工作。
 */
@Component("internalAccountProvider")
public class InternalAccountProvider implements AccountProvider {
    private static final AccountUserType USER_TYPE = AccountUserType.ADMIN;

    @Resource(name = "internalUserDao")
    private InternalUserDao internalUserDao;

    @Resource(name = "userCampusDao")
    private UserCampusDao userCampusDao;

    @Resource(name = "securityCacheService")
    private SecurityCacheService securityCacheService;

    @Override
    public String getUserType() {
        return USER_TYPE.getCode();
    }

    /**
     * 组装教务端 UserDetails。
     * 先读取权限缓存与 security 用户快照缓存，缓存未命中时再回源数据库并回填缓存。
     */
    @Override
    public UserDetails loadUserDetails(Long userId) {
        List<String> permissionCodes = securityCacheService.getAuthorityCodes(USER_TYPE.getCode(), userId);
        if (permissionCodes.isEmpty()) {
            permissionCodes = PermissionCodeResolver.resolvePermissionCodes(USER_TYPE);
            securityCacheService.cacheAuthorityCodes(USER_TYPE.getCode(), userId, permissionCodes);
        }
        List<SimpleGrantedAuthority> authorities = permissionCodes
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        if (authorities.isEmpty()) {
            throw new UsernameNotFoundException("No authority for internal user: " + userId);
        }

        SecurityCacheService.SecurityUserSnapshot userSnapshot =
                securityCacheService.getUserSnapshot(USER_TYPE.getCode(), userId);
        boolean enabled;
        Long currentCampusId;
        if (userSnapshot != null) {
            enabled = userSnapshot.enabled();
            currentCampusId = userSnapshot.currentCampusId();
        } else {
            InternalUserEntity internalUser = internalUserDao.getById(userId);
            if (internalUser == null) {
                throw new UsernameNotFoundException("Internal user not found: " + userId);
            }
            enabled = InternalUserStatus.ENABLED.equals(internalUser.getStatusEnum());
            currentCampusId = userCampusDao.getPrimaryCampusIdByUserId(userId);
            securityCacheService.cacheUserSnapshot(
                    new SecurityCacheService.SecurityUserSnapshot(USER_TYPE.getCode(), userId, enabled, currentCampusId)
            );
        }

        return new ChordSkedUserDetails(
                userId,
                USER_TYPE,
                currentCampusId,
                enabled,
                authorities
        );
    }
}
