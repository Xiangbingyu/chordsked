package com.chordsked.backend.exception;

public enum ErrorCode {
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证或令牌无效"),
    FORBIDDEN(403, "无权限访问"),
    DUPLICATE_REQUEST(409, "请勿重复操作"),
    INTERNAL_ERROR(500, "系统内部错误");

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
