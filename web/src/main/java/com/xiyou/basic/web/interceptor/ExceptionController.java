package com.xiyou.basic.web.interceptor;

import com.xiyou.basic.cache.common.exception.BizException;
import com.xiyou.basic.cache.common.exception.CommonErrorCode;
import com.xiyou.basic.web.result.WebResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.Collections;
import java.util.Set;

/**
 * Created by baiyc
 * 2020/1/20/020 17:29
 * Description：
 */
@Slf4j
@ControllerAdvice
public class ExceptionController {
    static final Set<MediaType> EXCEPTION_RESPONSE_MEDIA_TYPE = Collections.singleton(MediaType.APPLICATION_JSON_UTF8);

    @ExceptionHandler(BizException.class)
    @ResponseBody
    public WebResult businessException(BizException exception, HttpServletRequest request) {
        request.setAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE, EXCEPTION_RESPONSE_MEDIA_TYPE);
        WebResult webResult = WebResult.fail(exception.getErrorCode(), exception.getMessage());
        return webResult;
    }

    @ExceptionHandler(BindException.class)
    @ResponseBody
    public WebResult bindException(BindException exception, HttpServletRequest request) {
        request.setAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE, EXCEPTION_RESPONSE_MEDIA_TYPE);
        String tipsMsg = bindExceptionMsg(exception);
        WebResult webResult = WebResult.fail(CommonErrorCode.PARAM_EXCEPTION, tipsMsg);
        return webResult;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public WebResult methodArgumentNotValidException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        request.setAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE, EXCEPTION_RESPONSE_MEDIA_TYPE);
        String tipsMsg = bindExceptionMsg(exception.getBindingResult());
        WebResult webResult = WebResult.fail(CommonErrorCode.PARAM_EXCEPTION, tipsMsg);
        return webResult;
    }

    @ResponseBody
    @ExceptionHandler(ConstraintViolationException.class)
    public WebResult handleApiConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        request.setAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE, EXCEPTION_RESPONSE_MEDIA_TYPE);
        String tipsMsg = ex.getConstraintViolations().iterator().next().getMessage();
        WebResult webResult = WebResult.fail(CommonErrorCode.PARAM_EXCEPTION, tipsMsg);
        return webResult;

    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public WebResult runtimeException(RuntimeException exception, HttpServletRequest request) {
        request.setAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE, EXCEPTION_RESPONSE_MEDIA_TYPE);
        log.error("未捕获的异常!", exception);
        WebResult webResult = WebResult.fail(CommonErrorCode.COMMON_BIZ_EXCEPTION, "稍后重试!");
        return webResult;
    }


    private String bindExceptionMsg(BindingResult bindingResult) {
        String tipsMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
        return tipsMsg;
    }
}
