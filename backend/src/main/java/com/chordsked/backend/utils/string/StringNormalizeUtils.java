package com.chordsked.backend.utils.string;

import java.util.Locale;

public final class StringNormalizeUtils {
    private StringNormalizeUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 将字符串标准化为去除首尾空白后的大写形式；null 输入返回空字符串。
     */
    public static String trimToUpperCaseOrEmpty(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
