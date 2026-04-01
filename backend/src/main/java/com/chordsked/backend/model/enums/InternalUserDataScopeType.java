package com.chordsked.backend.model.enums;

public enum InternalUserDataScopeType {
    ALL_COMPANY(1),
    DEPARTMENT_AND_SUBORDINATE(2),
    SELF_ONLY(3),
    SPECIFIED_CAMPUS(4);

    private final int code;

    InternalUserDataScopeType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static InternalUserDataScopeType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (InternalUserDataScopeType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
