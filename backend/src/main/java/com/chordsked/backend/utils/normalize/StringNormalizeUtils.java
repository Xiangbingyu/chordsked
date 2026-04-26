package com.chordsked.backend.utils.normalize;

import java.util.Locale;

public final class StringNormalizeUtils {
    private StringNormalizeUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Normalize a string by trimming and converting to upper case; null returns an empty string.
     */
    public static String normalizeOrEmpty(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}

