package com.chordsked.backend.model.enums;

public enum AccountUserType {
    ADMIN("ADMIN"),
    TEACHER("TEACHER"),
    STUDENT("STUDENT");

    private final String code;

    AccountUserType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static AccountUserType fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("User type must not be blank");
        }
        for (AccountUserType type : values()) {
            if (type.code.equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported user type: " + value);
    }
}
