package com.chordsked.backend.model.enums;

public enum TeacherUserLevelType {
    JUNIOR(1),
    INTERMEDIATE(2),
    SENIOR(3),
    EXPERT(4);

    private final int code;

    TeacherUserLevelType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static TeacherUserLevelType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TeacherUserLevelType level : values()) {
            if (level.code == code) {
                return level;
            }
        }
        return null;
    }
}
