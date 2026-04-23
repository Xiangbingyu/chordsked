package com.chordsked.backend.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component("dataScopeProperties")
@Validated
@ConfigurationProperties(prefix = "chordsked.security.datascope")
public class DataScopeProperties {
    private boolean enabled = true;
    private boolean strictPlaceholder = true;

    @NotBlank
    private String sqlPlaceholder = "/*DATA_SCOPE*/";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isStrictPlaceholder() {
        return strictPlaceholder;
    }

    public void setStrictPlaceholder(boolean strictPlaceholder) {
        this.strictPlaceholder = strictPlaceholder;
    }

    public String getSqlPlaceholder() {
        return sqlPlaceholder;
    }

    public void setSqlPlaceholder(String sqlPlaceholder) {
        this.sqlPlaceholder = sqlPlaceholder;
    }
}
