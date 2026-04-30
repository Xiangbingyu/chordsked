package com.chordsked.backend.model.enums;

public enum OrgNodeType {
    CAMPUS(1),
    DEPT(2),
    GROUP(3);

    private final int code;

    OrgNodeType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public boolean isCampus() {
        return this == CAMPUS;
    }

    public boolean isDept() {
        return this == DEPT;
    }

    public boolean isGroup() {
        return this == GROUP;
    }

    public static OrgNodeType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrgNodeType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
