package com.chordsked.backend.security.account.provider;

import org.springframework.security.core.userdetails.UserDetails;

public interface AccountProvider {
    /**
     * 返回当前 Provider 负责的账号类型（如 ADMIN/TEACHER/STUDENT）。
     */
    String getUserType();

    /**
     * 基于 userId 装载 UserDetails。
     * 当前可以是静态权限实现，后续可替换为数据库/缓存查询实现。
     */
    UserDetails getUserDetails(String userId);
}
