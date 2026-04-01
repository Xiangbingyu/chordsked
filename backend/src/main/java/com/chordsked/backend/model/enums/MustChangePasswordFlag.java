package com.chordsked.backend.model.enums;

public enum MustChangePasswordFlag {
    NO(0),
    YES(1);

    private final int code;

    MustChangePasswordFlag(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static MustChangePasswordFlag fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MustChangePasswordFlag value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
