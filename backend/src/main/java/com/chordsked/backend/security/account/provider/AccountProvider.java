package com.chordsked.backend.security.account.provider;

import org.springframework.security.core.userdetails.UserDetails;

public interface AccountProvider {
    /**
     * 返回当前 Provider 负责的账号类型（如 ADMIN/TEACHER/STUDENT）。
     */
    String getUserType();

    /**
     * 基于 userId 装载 UserDetails。
     * 注意：权限码建议统一为“端前缀:资源:动作”风格（如 admin:user:view），
     * 用于与三端 API 前缀隔离策略保持一致。
     */
    UserDetails getUserDetails(Long userId);
}
