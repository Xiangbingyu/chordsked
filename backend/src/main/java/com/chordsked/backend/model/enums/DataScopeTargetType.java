package com.chordsked.backend.model.enums;

public enum DataScopeTargetType {
    ROLE,
    USER;

    public static DataScopeTargetType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (DataScopeTargetType type : values()) {
            if (type.name().equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        return null;
    }
}
