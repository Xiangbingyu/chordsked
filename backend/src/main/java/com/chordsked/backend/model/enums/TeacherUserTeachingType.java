package com.chordsked.backend.model.enums;

public enum TeacherUserTeachingType {
    ONE_ON_ONE,
    SMALL,
    LARGE,
    ALL;

    public static TeacherUserTeachingType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (TeacherUserTeachingType type : values()) {
            if (type.name().equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        return null;
    }
}
