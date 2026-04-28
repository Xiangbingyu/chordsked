package com.chordsked.backend.config.properties;

import com.chordsked.backend.utils.normalize.CodeNormalizeUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "chordsked.role")
@Component("roleProperties")
public class RoleProperties {
    private static final List<String> DEFAULT_PROTECTED_ROLE_CODES = List.of("SYSTEM_ADMIN");

    private List<String> protectedRoleCodes = DEFAULT_PROTECTED_ROLE_CODES;

    public List<String> getProtectedRoleCodes() {
        return protectedRoleCodes;
    }

    public void setProtectedRoleCodes(List<String> protectedRoleCodes) {
        this.protectedRoleCodes = normalizeRoleCodes(protectedRoleCodes, DEFAULT_PROTECTED_ROLE_CODES);
    }

    private List<String> normalizeRoleCodes(List<String> roleCodes, List<String> defaultRoleCodes) {
        List<String> normalizedRoleCodes = CodeNormalizeUtils.normalizeCodes(roleCodes);
        return normalizedRoleCodes.isEmpty() ? defaultRoleCodes : normalizedRoleCodes;
    }
}
