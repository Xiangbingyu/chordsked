package com.chordsked.backend.security.account.provider;

import com.chordsked.backend.cache.security.SecurityCacheService;
import com.chordsked.backend.dao.TeacherUserDao;
import com.chordsked.backend.model.entity.TeacherUserEntity;
import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.model.enums.TeacherUserStatus;
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
 * 教师端账号 Provider。
 * 负责根据 JWT 中的 userId 装载教师端 UserDetails，并与权限缓存、用户快照缓存协同工作。
 */
@Component("teacherAccountProvider")
public class TeacherAccountProvider implements AccountProvider {
    private static final AccountUserType USER_TYPE = AccountUserType.TEACHER;
    private static final UserDataScopeType DEFAULT_DATA_SCOPE_TYPE = UserDataScopeType.ASSIGNED;

    @Resource(name = "teacherUserDao")
    private TeacherUserDao teacherUserDao;

    @Resource(name = "securityCacheService")
    private SecurityCacheService securityCacheService;

    @Resource(name = "authorityCodeService")
    private AuthorityCodeService authorityCodeService;

    @Override
    public String getUserType() {
        return USER_TYPE.getCode();
    }

    /**
     * 组装教师端 UserDetails。
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
            throw new UsernameNotFoundException("No authority for teacher user: " + userId);
        }
        SecurityCacheService.SecurityUserSnapshot userSnapshot =
                securityCacheService.getUserSnapshot(USER_TYPE.getCode(), userId);
        boolean enabled;
        Long currentCampusId;
        if (userSnapshot != null) {
            enabled = userSnapshot.enabled();
            currentCampusId = userSnapshot.currentCampusId();
        } else {
            TeacherUserEntity teacherUser = teacherUserDao.getById(userId);
            if (teacherUser == null) {
                throw new UsernameNotFoundException("Teacher user not found: " + userId);
            }
            enabled = TeacherUserStatus.ON_DUTY.equals(teacherUser.getStatusEnum());
            currentCampusId = teacherUser.getCampusId();
            securityCacheService.cacheUserSnapshot(
                    new SecurityCacheService.SecurityUserSnapshot(
                            USER_TYPE.getCode(),
                            userId,
                            enabled,
                            currentCampusId,
                            null,
                            DEFAULT_DATA_SCOPE_TYPE
                    )
            );
        }
        return new ChordSkedUserDetails(
                userId,
                USER_TYPE,
                currentCampusId,
                null,
                userSnapshot == null || userSnapshot.dataScopeType() == null
                        ? DEFAULT_DATA_SCOPE_TYPE
                        : userSnapshot.dataScopeType(),
                enabled,
                authorities
        );
    }
}
