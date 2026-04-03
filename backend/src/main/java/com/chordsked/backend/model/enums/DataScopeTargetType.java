package com.chordsked.backend.model.enums;

public enum DataScopeTargetType {
    ROLE(1),
    USER(2);

    private final int code;

    DataScopeTargetType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static DataScopeTargetType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DataScopeTargetType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
