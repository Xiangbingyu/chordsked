package com.chordsked.backend.model.enums;

public enum TeacherUserStatus {
    ON_DUTY(1),
    LEAVED(2),
    DISABLED(3);

    private final int code;

    TeacherUserStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static TeacherUserStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TeacherUserStatus value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
