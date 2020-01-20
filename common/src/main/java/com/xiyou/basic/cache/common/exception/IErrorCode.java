package com.xiyou.basic.cache.common.exception;

/**
 * Created by baiyc
 * 2020/1/19/019 17:12
 * Description：自定义异常接口
 */
public interface IErrorCode {
    /**异常码*/
    int code();
    /**异常信息*/
    String message();
}
