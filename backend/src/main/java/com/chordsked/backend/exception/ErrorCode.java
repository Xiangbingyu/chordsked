package com.chordsked.backend.exception;

public enum ErrorCode {
    BAD_REQUEST(40000, "请求参数错误"),
    INTERNAL_ERROR(50000, "系统内部错误");

    private final String message;
    private final int code;

    ErrorCode(int code, String message) {
        this.message = message;
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }
}
