package com.xiyou.basic.cache;

import com.xiyou.basic.cache.exception.DulplicateCacheNameException;
import org.springframework.beans.BeansException;
import org.springframework.cache.Cache;
import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by baiyc
 * 2020/1/19/019 17:47
 * Description：缓存bean注册类
 */
public class CacheBeanRegisterPostProcessor {
    private List<String> cacheNames = new LinkedList();

    public CacheBeanRegisterPostProcessor() {
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof Cache) {
            Cache cache = (Cache)bean;
            String cacheName = cache.getName();
            if(!StringUtils.hasText(cacheName)) {
                return bean;
            }

            if(this.cacheNames.contains(cacheName)) {
                throw new DulplicateCacheNameException("发现重复的缓存名 " + cacheName);
            }

            this.cacheNames.add(cacheName);
        }

        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
