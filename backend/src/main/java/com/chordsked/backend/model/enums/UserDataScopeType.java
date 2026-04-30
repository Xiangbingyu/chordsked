package com.chordsked.backend.model.enums;

public enum UserDataScopeType {
    ALL(1),
    ASSIGNED(2),
    SELF(3);

    private final int code;

    UserDataScopeType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public boolean isAllScope() {
        return this == ALL;
    }

    public boolean isAssignedScope() {
        return this == ASSIGNED;
    }

    public boolean isSelfScope() {
        return this == SELF;
    }

    public static UserDataScopeType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserDataScopeType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
