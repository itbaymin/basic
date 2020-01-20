package com.xiyou.basic.cache.configuration;

import com.xiyou.basic.cache.CacheBeanRegisterPostProcessor;
import com.xiyou.basic.cache.Lock;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Created by baiyc
 * 2020/1/19/019 19:35
 * Description：配置
 */
@EnableCaching(proxyTargetClass = true)
@ComponentScan(basePackages = "com.xiyou.basic.cache")
@Configuration
public class CacheAutoConfiguration {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public CacheBeanRegisterPostProcessor cacheBeanRegisterPostProcessor() {
        return new CacheBeanRegisterPostProcessor();
    }

    @Bean
    public Lock redisLock(StringRedisTemplate stringRedisTemplate) {
        return new Lock(stringRedisTemplate);
    }
}
