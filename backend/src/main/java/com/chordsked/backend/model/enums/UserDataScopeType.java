package com.chordsked.backend.model.enums;

public enum UserDataScopeType {
    ALL_COMPANY(1),
    SELF_ONLY(3),
    SPECIFIED_CAMPUS(4);

    private final int code;

    UserDataScopeType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public boolean isAllScope() {
        return this == ALL_COMPANY;
    }

    public boolean isCampusScope() {
        return this == SPECIFIED_CAMPUS;
    }

    public boolean isSelfScope() {
        return this == SELF_ONLY;
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
