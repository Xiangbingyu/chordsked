package com.chordsked.backend.model.enums;

public enum AuditLogModule {
    ROLE_MANAGEMENT("ROLE_MANAGEMENT"),
    INTERNAL_USER_MANAGEMENT("INTERNAL_USER_MANAGEMENT");

    private final String code;

    AuditLogModule(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
