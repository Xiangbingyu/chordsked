package com.chordsked.backend.security.account.service;

import com.chordsked.backend.security.account.provider.AccountProvider;
import com.chordsked.backend.utils.normalize.StringNormalizeUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service("multiAccountUserDetailsService")
public class MultiAccountUserDetailsService implements UserDetailsService {
    // key: 鏍囧噯鍖栧悗鐨?userType锛堝ぇ鍐欙級锛寁alue: 瀵瑰簲璐﹀彿浣撶郴鐨勬潈闄愯杞藉疄鐜?
    private final Map<String, AccountProvider> providers;

    public MultiAccountUserDetailsService(List<AccountProvider> accountProviders) {
        Map<String, AccountProvider> providerMap = new ConcurrentHashMap<>();
        // 鍚姩鏃跺皢鍏ㄩ儴 Provider 娉ㄥ唽鍒板唴瀛樿矾鐢辫〃锛岄伩鍏嶆瘡娆¤姹傞亶鍘嗘煡鎵?
        // 濡傚悗缁柊澧炶处鍙蜂綋绯伙紝浠呴渶鏂板涓€涓?AccountProvider 瀹炵幇鍗冲彲鑷姩鎺ュ叆
        for (AccountProvider accountProvider : accountProviders) {
            providerMap.put(StringNormalizeUtils.normalizeOrEmpty(accountProvider.getUserType()), accountProvider);
        }
        this.providers = providerMap;
    }

    /**
     * 鍩轰簬 JWT 涓殑 userType + userId 杩涜璐﹀彿浣撶郴璺敱骞惰杞?UserDetails銆?
     * 鍚庣画鎺ュ叆鏁版嵁搴?缂撳瓨鏃讹紝浠呴渶鏀归€犲叿浣?Provider 瀹炵幇銆?
     */
    public UserDetails loadUserDetailsByTokenContext(String userType, Long userId) {
        if (userId == null || userId <= 0) {
            throw new UsernameNotFoundException("Invalid user id");
        }
        AccountProvider accountProvider = providers.get(StringNormalizeUtils.normalizeOrEmpty(userType));
        if (accountProvider == null) {
            throw new UsernameNotFoundException("Unsupported user type: " + userType);
        }
        return accountProvider.loadUserDetails(userId);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 褰撳墠绯荤粺缁熶竴璧?JWT 涓婁笅鏂囪矾鐢憋紝涓嶆敮鎸?username 鐩磋繛鏌ヨ
        throw new UsernameNotFoundException("Username based lookup is not supported");
    }
}

