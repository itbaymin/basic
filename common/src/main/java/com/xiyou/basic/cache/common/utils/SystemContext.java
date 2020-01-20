package com.xiyou.basic.cache.common.utils;


import java.util.Optional;

/**
 * Created by baiyc
 * 2020/1/19/019 16:30
 * Description：上下文
 */
public class SystemContext {
    private static ThreadLocal<SystemInfo> threadLocal = new ThreadLocal();
    private static final SystemInfo EMPTY = new SystemInfo();

    public SystemContext() {
    }

    public static void set(SystemInfo object) {
        if(object != null) {
            threadLocal.set(object);
        }

    }

    public static SystemInfo get() {
        return threadLocal.get();
    }

    public static void remove() {
        threadLocal.remove();
    }

    public static String getRequestIp() {
        SystemInfo systemInfo = Optional.ofNullable(get()).orElse(EMPTY);
        return systemInfo.getRequestIp();
    }
}
