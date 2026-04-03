package com.chordsked.backend.model.enums;

public enum LoginLogUserType {
    ADMIN(1, AccountUserType.ADMIN),
    TEACHER(2, AccountUserType.TEACHER),
    STUDENT(3, AccountUserType.STUDENT);

    private final int code;
    private final AccountUserType accountUserType;

    LoginLogUserType(int code, AccountUserType accountUserType) {
        this.code = code;
        this.accountUserType = accountUserType;
    }

    public int getCode() {
        return code;
    }

    public AccountUserType getAccountUserType() {
        return accountUserType;
    }

    public static LoginLogUserType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (LoginLogUserType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }

    public static LoginLogUserType fromAccountUserType(AccountUserType accountUserType) {
        if (accountUserType == null) {
            return null;
        }
        for (LoginLogUserType type : values()) {
            if (type.accountUserType == accountUserType) {
                return type;
            }
        }
        return null;
    }
}
