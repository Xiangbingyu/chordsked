package com.chordsked.backend.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "chordsked.role")
@Component("roleProperties")
public class RoleProperties {
    private List<String> presetRoleCodes = List.of("ADMIN", "OPERATOR");

    public List<String> getPresetRoleCodes() {
        return presetRoleCodes;
    }

    public void setPresetRoleCodes(List<String> presetRoleCodes) {
        this.presetRoleCodes = presetRoleCodes;
    }
}
