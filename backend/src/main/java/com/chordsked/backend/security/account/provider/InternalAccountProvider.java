package com.chordsked.backend.security.account.provider;

import com.chordsked.backend.security.account.model.ChordSkedUserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("internalAccountProvider")
public class InternalAccountProvider implements AccountProvider {
    private static final String USER_TYPE = "ADMIN";
    // 当前为静态权限占位实现，便于安全链路先跑通。
    // 后续接入数据库时，建议改为：根据 userId 查询用户 -> 查询角色 -> 查询权限点 -> 转换为 GrantedAuthority。
    private static final List<SimpleGrantedAuthority> AUTHORITIES = List.of(
            new SimpleGrantedAuthority("ROLE_ADMIN"),
            new SimpleGrantedAuthority("ADMIN"),
            new SimpleGrantedAuthority("user:view"),
            new SimpleGrantedAuthority("user:create"),
            new SimpleGrantedAuthority("user:update"),
            new SimpleGrantedAuthority("user:delete"),
            new SimpleGrantedAuthority("role:view"),
            new SimpleGrantedAuthority("role:create"),
            new SimpleGrantedAuthority("role:update"),
            new SimpleGrantedAuthority("role:delete")
    );

    @Override
    public String getUserType() {
        return USER_TYPE;
    }

    @Override
    public UserDetails getUserDetails(String userId) {
        // 数据库化改造示例：
        // 1) internalUserMapper.findById(userId)
        // 2) roleMapper.findByUserId(userId)
        // 3) permissionMapper.findCodesByUserId(userId)
        // 4) 将权限码映射为 SimpleGrantedAuthority 并返回 ChordSkedUserDetails
        return new ChordSkedUserDetails(userId, USER_TYPE, null, AUTHORITIES);
    }
}
