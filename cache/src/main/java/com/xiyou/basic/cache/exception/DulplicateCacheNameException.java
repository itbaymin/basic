package com.xiyou.basic.cache.exception;

/**
 * Created by baiyc
 * 2020/1/19/019 17:47
 * Description：重名异常
 */
public class DulplicateCacheNameException extends RuntimeException {
    public DulplicateCacheNameException(String message) {
        super(message);
    }
}
