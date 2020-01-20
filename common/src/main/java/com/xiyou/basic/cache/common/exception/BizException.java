package com.xiyou.basic.cache.common.exception;

/**
 * Created by baiyc
 * 2020/1/19/019 17:12
 * Description：业务异常
 */
public class BizException extends RuntimeException {
    private IErrorCode errorCode;

    public BizException(IErrorCode errorCode, String msg) {
        super(msg);
        this.errorCode = errorCode;
    }

    public BizException(IErrorCode errorCode, Exception e) {
        super(e);
        this.errorCode = errorCode;
    }

    public BizException(IErrorCode errorCode, Exception e, String message) {
        super(message, e);
        this.errorCode = errorCode;
    }

    public BizException(IErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    public IErrorCode getErrorCode() {
        return this.errorCode;
    }
}
