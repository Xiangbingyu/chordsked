package com.chordsked.backend.model.enums;

public enum DataScopeRangeType {
    ALL,
    SPECIFIED,
    CURRENT,
    SELF;

    public static DataScopeRangeType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (DataScopeRangeType type : values()) {
            if (type.name().equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        return null;
    }
}
