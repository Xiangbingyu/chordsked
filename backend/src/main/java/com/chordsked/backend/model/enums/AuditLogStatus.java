package com.chordsked.backend.model.enums;

public enum AuditLogStatus {
    FAILED(0),
    SUCCESS(1);

    private final int code;

    AuditLogStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static AuditLogStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AuditLogStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
