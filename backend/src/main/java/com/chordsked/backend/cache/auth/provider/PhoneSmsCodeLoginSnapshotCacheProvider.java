package com.chordsked.backend.cache.auth.provider;

import com.chordsked.backend.model.auth.AuthLoginSnapshot;
import com.chordsked.backend.model.auth.AuthLoginMethod;
import com.chordsked.backend.model.enums.AccountUserType;
import org.springframework.stereotype.Component;

/**
 * 手机号验证码登录快照缓存 provider。
 * 当前仅保留基础声明，待手机号验证码登录正式开放后再补充具体缓存读写逻辑。
 */
@Component("phoneSmsCodeLoginSnapshotCacheProvider")
public class PhoneSmsCodeLoginSnapshotCacheProvider implements AuthLoginSnapshotCacheProvider {
    @Override
    public AuthLoginMethod getLoginMethod() {
        return AuthLoginMethod.PHONE_SMS_CODE;
    }

    @Override
    /**
     * 当前阶段该登录方式缓存未开放，统一按未命中处理。
     */
    public AuthLoginSnapshot getLoginSnapshot(AccountUserType userType, String principal) {
        return null;
    }

    @Override
    /**
     * 当前阶段该登录方式缓存未开放，写入请求直接忽略。
     */
    public void cacheLoginSnapshot(AuthLoginSnapshot snapshot) {
    }

    @Override
    /**
     * 当前阶段该登录方式缓存未开放，清理请求直接忽略。
     */
    public void clearLoginSnapshot(AccountUserType userType, String principal) {
    }
}
