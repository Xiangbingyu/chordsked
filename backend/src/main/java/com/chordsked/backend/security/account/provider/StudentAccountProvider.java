package com.chordsked.backend.security.account.provider;

import com.chordsked.backend.security.account.model.ChordSkedUserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("studentAccountProvider")
public class StudentAccountProvider implements AccountProvider {
    private static final String USER_TYPE = "STUDENT";
    // 当前学员端为内置角色权限，后续如需细粒度控制可改为数据库加载权限点。
    private static final List<SimpleGrantedAuthority> AUTHORITIES = List.of(
            new SimpleGrantedAuthority("ROLE_STUDENT"),
            new SimpleGrantedAuthority("STUDENT")
    );

    @Override
    public String getUserType() {
        return USER_TYPE;
    }

    @Override
    public UserDetails getUserDetails(String userId) {
        // 后续数据库化可在此校验学员账号状态并动态装载权限。
        return new ChordSkedUserDetails(userId, USER_TYPE, null, AUTHORITIES);
    }
}
