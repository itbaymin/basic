package com.xiyou.basic.cache.common.exception;

/**
 * Created by baiyc
 * 2020/1/19/019 17:15
 * Description：异常码枚举
 */
public enum CommonErrorCode implements IErrorCode {
    PARAM_EXCEPTION(1000, "参数验证失败"),
    COMMON_BIZ_EXCEPTION(2000, "业务异常"),
    UNLOGIN_ERROR(3000, "未登录");

    private int code;
    private String message;

    public int code() {
        return this.code;
    }

    public String message() {
        return this.message;
    }

    private CommonErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
