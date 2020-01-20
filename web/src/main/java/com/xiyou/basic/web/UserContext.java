package com.xiyou.basic.web;

import java.util.Optional;

/**
 * Created by baiyc
 * 2020/1/20/020 17:33
 * Description：
 */
public class UserContext {
    public static ThreadLocal<Principle> threadLocal = new ThreadLocal();

    private static final Principle EMPTY = () -> "";

    public static void set(Principle object) {
        if (object != null) {
            threadLocal.set(object);
        }
    }

    public static <T extends Principle> T get(Class<T> userClass) {
        Principle principle = get();
        if(principle != null) {
            return (T) principle;
        }
        return null;
    }

    public static Principle get() {
        return threadLocal.get();
    }

    public static String getUserId() {
        return Optional.ofNullable(get()).orElse(EMPTY).id();
    }

    public static void remove() {
        threadLocal.remove();
    }
}
