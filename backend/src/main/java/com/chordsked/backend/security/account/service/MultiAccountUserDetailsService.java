package com.chordsked.backend.security.account.service;

import com.chordsked.backend.security.account.provider.AccountProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service("multiAccountUserDetailsService")
public class MultiAccountUserDetailsService implements UserDetailsService {
    // key: 标准化后的 userType（大写），value: 对应账号体系的权限装载实现
    private final Map<String, AccountProvider> providers;

    public MultiAccountUserDetailsService(List<AccountProvider> accountProviders) {
        Map<String, AccountProvider> providerMap = new ConcurrentHashMap<>();
        // 启动时将全部 Provider 注册到内存路由表，避免每次请求遍历查找
        // 如后续新增账号体系，仅需新增一个 AccountProvider 实现即可自动接入
        for (AccountProvider accountProvider : accountProviders) {
            providerMap.put(normalizeUserType(accountProvider.getUserType()), accountProvider);
        }
        this.providers = providerMap;
    }

    /**
     * 基于 JWT 中的 userType + userId 进行账号体系路由并装载 UserDetails。
     * 后续接入数据库/缓存时，仅需改造具体 Provider 实现。
     */
    public UserDetails loadUserDetailsByTokenContext(String userType, Long userId) {
        if (userId == null || userId <= 0) {
            throw new UsernameNotFoundException("Invalid user id");
        }
        AccountProvider accountProvider = providers.get(normalizeUserType(userType));
        if (accountProvider == null) {
            throw new UsernameNotFoundException("Unsupported user type: " + userType);
        }
        return accountProvider.loadUserDetails(userId);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 当前系统统一走 JWT 上下文路由，不支持 username 直连查询
        throw new UsernameNotFoundException("Username based lookup is not supported");
    }

    private String normalizeUserType(String userType) {
        // 统一处理大小写与空白，确保路由 key 稳定
        return Objects.requireNonNullElse(userType, "").trim().toUpperCase();
    }
}
