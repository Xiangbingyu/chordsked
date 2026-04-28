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
    // key: normalized uppercase userType, value: matching account provider
    private final Map<String, AccountProvider> providers;

    public MultiAccountUserDetailsService(List<AccountProvider> accountProviders) {
        Map<String, AccountProvider> providerMap = new ConcurrentHashMap<>();
        // Register all providers once at startup to avoid scanning the list on every request.
        // New account systems only need to add another AccountProvider implementation.
        for (AccountProvider accountProvider : accountProviders) {
            providerMap.put(StringNormalizeUtils.normalizeOrEmpty(accountProvider.getUserType()), accountProvider);
        }
        this.providers = providerMap;
    }

    /**
     * Load user details from JWT token context by userType and userId.
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
        // The current system only supports lookup through JWT token context.
        throw new UsernameNotFoundException("Username based lookup is not supported");
    }
}