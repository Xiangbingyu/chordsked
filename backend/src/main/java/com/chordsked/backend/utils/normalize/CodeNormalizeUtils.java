package com.chordsked.backend.utils.normalize;

import java.util.List;
import java.util.Objects;

public final class CodeNormalizeUtils {
    private CodeNormalizeUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Normalize a code value by trimming and converting to upper case; null returns an empty string.
     */
    public static String normalizeCodeOrEmpty(String value) {
        return StringNormalizeUtils.normalizeOrEmpty(value);
    }

    /**
     * Normalize code values by trimming, converting to upper case, removing blanks and duplicates.
     */
    public static List<String> normalizeCodes(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .filter(Objects::nonNull)
                .map(CodeNormalizeUtils::normalizeCodeOrEmpty)
                .filter(code -> !code.isEmpty())
                .distinct()
                .toList();
    }
}

