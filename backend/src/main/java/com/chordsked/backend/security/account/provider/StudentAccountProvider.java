package com.chordsked.backend.security.account.provider;

import com.chordsked.backend.cache.security.SecurityCacheService;
import com.chordsked.backend.dao.StudentUserDao;
import com.chordsked.backend.model.entity.StudentUserEntity;
import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.model.enums.StudentUserStatus;
import com.chordsked.backend.model.enums.UserDataScopeType;
import com.chordsked.backend.service.security.AuthorityCodeService;
import com.chordsked.backend.security.account.model.ChordSkedUserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学员端账号 Provider。
 * 负责根据 JWT 中的 userId 装载学员端 UserDetails，并与权限缓存、用户快照缓存协同工作。
 */
@Component("studentAccountProvider")
public class StudentAccountProvider implements AccountProvider {
    private static final AccountUserType USER_TYPE = AccountUserType.STUDENT;
    private static final UserDataScopeType DEFAULT_DATA_SCOPE_TYPE = UserDataScopeType.SPECIFIED_CAMPUS;

    @Resource(name = "studentUserDao")
    private StudentUserDao studentUserDao;

    @Resource(name = "securityCacheService")
    private SecurityCacheService securityCacheService;

    @Resource(name = "authorityCodeService")
    private AuthorityCodeService authorityCodeService;

    @Override
    public String getUserType() {
        return USER_TYPE.getCode();
    }

    /**
     * 组装学员端 UserDetails。
     * 先读取权限缓存与 security 用户快照缓存，缓存未命中时再回源数据库并回填缓存。
     */
    @Override
    public UserDetails loadUserDetails(Long userId) {
        List<String> permissionCodes = securityCacheService.getAuthorityCodes(USER_TYPE.getCode(), userId);
        if (permissionCodes.isEmpty()) {
            permissionCodes = authorityCodeService.getAuthorityCodes(USER_TYPE, userId);
            securityCacheService.cacheAuthorityCodes(USER_TYPE.getCode(), userId, permissionCodes);
        }
        List<SimpleGrantedAuthority> authorities = permissionCodes
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        if (authorities.isEmpty()) {
            throw new UsernameNotFoundException("No authority for student user: " + userId);
        }
        SecurityCacheService.SecurityUserSnapshot userSnapshot =
                securityCacheService.getUserSnapshot(USER_TYPE.getCode(), userId);
        boolean enabled;
        Long currentCampusId;
        if (userSnapshot != null) {
            enabled = userSnapshot.enabled();
            currentCampusId = userSnapshot.currentCampusId();
        } else {
            StudentUserEntity studentUser = studentUserDao.getById(userId);
            if (studentUser == null) {
                throw new UsernameNotFoundException("Student user not found: " + userId);
            }
            enabled = StudentUserStatus.ENABLED.equals(studentUser.getStatusEnum());
            currentCampusId = studentUser.getCampusId();
            securityCacheService.cacheUserSnapshot(
                    new SecurityCacheService.SecurityUserSnapshot(
                            USER_TYPE.getCode(),
                            userId,
                            enabled,
                            currentCampusId,
                            DEFAULT_DATA_SCOPE_TYPE
                    )
            );
        }
        return new ChordSkedUserDetails(
                userId,
                USER_TYPE,
                currentCampusId,
                userSnapshot == null || userSnapshot.dataScopeType() == null
                        ? DEFAULT_DATA_SCOPE_TYPE
                        : userSnapshot.dataScopeType(),
                enabled,
                authorities
        );
    }
}
