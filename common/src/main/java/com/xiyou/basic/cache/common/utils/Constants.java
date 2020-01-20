package com.xiyou.basic.cache.common.utils;

/**
 * Created by baiyc
 * 2020/1/19/019 16:32
 * Description：常量
 */
public interface Constants {
    String GLOBAL_TRACE_KEY = "X-B3-TraceId";

    public interface Profile {
        String LOCAL = "local";
        String TEST = "test";
        String PROD = "prod";
    }
}
