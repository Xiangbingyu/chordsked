package com.chordsked.backend.model.enums;

public enum TeacherUserLevelType {
    JUNIOR,
    INTERMEDIATE,
    SENIOR,
    EXPERT;

    public static TeacherUserLevelType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (TeacherUserLevelType level : values()) {
            if (level.name().equalsIgnoreCase(value.trim())) {
                return level;
            }
        }
        return null;
    }
}
