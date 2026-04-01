package com.chordsked.backend.security.account.provider;

import com.chordsked.backend.cache.SecurityCacheService;
import com.chordsked.backend.dao.mapper.SecurityAccountMapper;
import com.chordsked.backend.model.entity.StudentUserEntity;
import com.chordsked.backend.model.enums.StudentUserStatus;
import com.chordsked.backend.security.account.model.ChordSkedUserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component("studentAccountProvider")
public class StudentAccountProvider implements AccountProvider {
    private static final String USER_TYPE = "STUDENT";
    private static final String ROLE_PERMISSION_CODE = "student:role";

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
        StudentUserEntity studentUser = securityAccountMapper.selectStudentUserById(userId);
        if (studentUser == null) {
            throw new UsernameNotFoundException("Student user not found: " + userId);
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
            throw new UsernameNotFoundException("No authority for student user: " + userId);
        }
        boolean enabled = StudentUserStatus.ENABLED.equals(studentUser.getStatusEnum());
        return new ChordSkedUserDetails(
                userId,
                USER_TYPE,
                studentUser.getCampusId(),
                true,
                enabled,
                authorities
        );
    }
}
