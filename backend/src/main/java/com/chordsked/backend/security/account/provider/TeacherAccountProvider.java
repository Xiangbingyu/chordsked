package com.chordsked.backend.security.account.provider;

import com.chordsked.backend.cache.SecurityCacheService;
import com.chordsked.backend.dao.mapper.SecurityAccountMapper;
import com.chordsked.backend.model.entity.TeacherUserEntity;
import com.chordsked.backend.model.enums.TeacherUserStatus;
import com.chordsked.backend.security.account.model.ChordSkedUserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component("teacherAccountProvider")
public class TeacherAccountProvider implements AccountProvider {
    private static final String USER_TYPE = "TEACHER";
    private static final String ROLE_PERMISSION_CODE = "teacher:role";

    @Resource(name = "securityAccountMapper")
    private SecurityAccountMapper securityAccountMapper;

    @Resource(name = "securityCacheService")
    private SecurityCacheService securityCacheService;

    @Override
    public String getUserType() {
        return USER_TYPE;
    }

    @Override
    public UserDetails getUserDetails(Long userId) {
        TeacherUserEntity teacherUser = securityAccountMapper.selectTeacherUserById(userId);
        if (teacherUser == null) {
            throw new UsernameNotFoundException("Teacher user not found: " + userId);
        }
        List<String> permissionCodes = securityCacheService.getAuthorityCodes(USER_TYPE, userId);
        if (permissionCodes.isEmpty()) {
            permissionCodes = securityAccountMapper.selectExistingPermissionCodes(List.of(ROLE_PERMISSION_CODE));
            securityCacheService.cacheAuthorityCodes(USER_TYPE, userId, permissionCodes);
        }
        List<SimpleGrantedAuthority> authorities = permissionCodes
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        if (authorities.isEmpty()) {
            throw new UsernameNotFoundException("No authority for teacher user: " + userId);
        }
        boolean accountNonLocked = teacherUser.getLockedUntil() == null || teacherUser.getLockedUntil().isBefore(LocalDateTime.now());
        boolean enabled = TeacherUserStatus.ON_DUTY.equals(teacherUser.getStatusEnum());
        return new ChordSkedUserDetails(
                userId,
                USER_TYPE,
                teacherUser.getCampusId(),
                accountNonLocked,
                enabled,
                authorities
        );
    }
}
