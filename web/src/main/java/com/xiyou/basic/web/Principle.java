package com.xiyou.basic.web;

/**
 * Created by baiyc
 * 2020/1/20/020 17:31
 * Description：Principle接口
 */
public interface Principle {
    String id();

    default String name(){
        return "";
    }
}
