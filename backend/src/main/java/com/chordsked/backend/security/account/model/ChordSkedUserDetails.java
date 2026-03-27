package com.chordsked.backend.security.account.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class ChordSkedUserDetails implements UserDetails {
    private final Long userId;
    private final String userType;
    private final Long currentCampusId;
    private final Collection<? extends GrantedAuthority> authorities;

    public ChordSkedUserDetails(
            Long userId,
            String userType,
            Long currentCampusId,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.userId = userId;
        this.userType = userType;
        this.currentCampusId = currentCampusId;
        this.authorities = authorities;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserType() {
        return userType;
    }

    public Long getCurrentCampusId() {
        return currentCampusId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        // 当前阶段认证链路基于 JWT claims，不在 UserDetails 中承载明文/密文密码。
        // 后续接入数据库密码校验流程时，再按真实账户字段返回或改造该实现。
        return "";
    }

    @Override
    public String getUsername() {
        // 当前鉴权主键统一使用 userId，先适配 Spring Security 的 String username 接口。
        // 后续接入数据库账号标识（如 internal.username / teacher.phone / teacher_no / student.phone）后再改为真实登录标识。
        return String.valueOf(userId);
    }

    @Override
    public boolean isAccountNonExpired() {
        // 当前为静态权限占位，后续接入数据库时需检查账号过期时间
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // 当前为静态权限占位，后续需实现登录失败锁定逻辑
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 当前为静态权限占位，后续需实现密码过期策略
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 当前为静态权限占位，后续需检查账号 ACTIVE/INACTIVE 状态
        return true;
    }
}
