package com.xiyou.basic.remote.annotation;

import java.lang.annotation.*;

/**
 * Created by baiyc
 * 2020/1/19/019 20:10
 * Description：
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface HeaderParam {
    /**
     * 头信息的Name, 不填写则默认使用字段名称
     */
    String value() default "";
}
