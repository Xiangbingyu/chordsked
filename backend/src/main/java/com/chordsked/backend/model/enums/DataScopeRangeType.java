package com.chordsked.backend.model.enums;

public enum DataScopeRangeType {
    ALL(1),
    SPECIFIED(2),
    CURRENT(3),
    SELF(4);

    private final int code;

    DataScopeRangeType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static DataScopeRangeType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DataScopeRangeType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
