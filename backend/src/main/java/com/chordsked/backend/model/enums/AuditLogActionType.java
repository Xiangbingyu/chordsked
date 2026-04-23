package com.chordsked.backend.model.enums;

public enum AuditLogActionType {
    CREATE_ROLE("CREATE_ROLE"),
    UPDATE_ROLE("UPDATE_ROLE"),
    DELETE_ROLE("DELETE_ROLE"),
    CREATE_INTERNAL_USER("CREATE_INTERNAL_USER");

    private final String code;

    AuditLogActionType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
