package com.chordsked.backend.security.account.provider;

import com.chordsked.backend.cache.SecurityCacheService;
import com.chordsked.backend.dao.mapper.SecurityAccountMapper;
import com.chordsked.backend.model.entity.InternalUserEntity;
import com.chordsked.backend.model.enums.InternalUserStatus;
import com.chordsked.backend.security.account.model.ChordSkedUserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component("internalAccountProvider")
public class InternalAccountProvider implements AccountProvider {
    private static final String USER_TYPE = "ADMIN";

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
        InternalUserEntity internalUser = securityAccountMapper.selectInternalUserById(userId);
        if (internalUser == null) {
            throw new UsernameNotFoundException("Internal user not found: " + userId);
        }

        List<String> permissionCodes = securityCacheService.getAuthorityCodes(USER_TYPE, userId);
        if (permissionCodes.isEmpty()) {
            permissionCodes = securityAccountMapper.selectPermissionCodesByInternalUserId(userId);
            securityCacheService.cacheAuthorityCodes(USER_TYPE, userId, permissionCodes);
        }
        List<SimpleGrantedAuthority> authorities = permissionCodes
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        if (authorities.isEmpty()) {
            throw new UsernameNotFoundException("No authority for internal user: " + userId);
        }

        boolean accountNonLocked = internalUser.getLockedUntil() == null || internalUser.getLockedUntil().isBefore(LocalDateTime.now());
        boolean enabled = InternalUserStatus.ENABLED.equals(internalUser.getStatusEnum());
        Long currentCampusId = securityAccountMapper.selectPrimaryCampusIdByInternalUserId(userId);

        return new ChordSkedUserDetails(
                userId,
                USER_TYPE,
                currentCampusId,
                accountNonLocked,
                enabled,
                authorities
        );
    }
}
