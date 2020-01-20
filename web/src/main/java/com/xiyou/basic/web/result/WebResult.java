package com.xiyou.basic.web.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xiyou.basic.cache.common.exception.CommonErrorCode;
import com.xiyou.basic.cache.common.exception.IErrorCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by baiyc
 * 2020/1/20/020 17:26
 * Description：响应对象
 */
public class WebResult<T> {
    public static final WebResult UN_LOGIN = WebResult.fail(CommonErrorCode.UNLOGIN_ERROR);

    @Getter
    @JsonProperty("status")
    private int code = 200;
    @Getter
    private String message = "";
    @Getter
    private T data;
    @Setter
    @Getter
    @JsonProperty("msg_key")
    private String msgKey = "";

    private WebResult(T data) {
        this.data = data;
    }

    private WebResult(T data, int code) {
        this(data);
        this.code = code;
    }

    public static <T> WebResult success(T data) {
        return new WebResult(data);
    }

    public static WebResult<String> fail(IErrorCode errorCode) {
        return fail(errorCode, "");
    }

    public static WebResult<String> fail(IErrorCode errorCode, String errorMessage) {
        WebResult failResult = new WebResult("", errorCode.code());
        failResult.message = StringUtils.defaultIfBlank(errorMessage, errorCode.message());
        return failResult;
    }
}
