package com.xiyou.basic.web.annotations;

import java.lang.annotation.*;

/**
 * Created by baiyc
 * 2020/1/20/020 17:25
 * Description：标记接口或控制器，不需进行loginFilter
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface NotLogin {
}
