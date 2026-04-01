package com.chordsked.backend.model.enums;

public enum LoginLogUserType {
    INTERNAL,
    TEACHER,
    STUDENT;

    public static LoginLogUserType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (LoginLogUserType type : values()) {
            if (type.name().equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        return null;
    }
}
