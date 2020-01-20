package com.xiyou.basic.cache.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.xiyou.basic.cache.AbstractCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.*;

/**
 * Created by baiyc
 * 2020/1/19/019 19:33
 * Description：缓存配置类
 */
@Slf4j
@Configuration
public class CacheManagerConfiguration {

    private List<AbstractCache> caches;

    ObjectMapper objectMapper = new ObjectMapper() {{
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }};
    JdkSerializationRedisSerializer jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer();
    static final StringRedisSerializer DEFAULT_KEY_SERIALIZER = new StringRedisSerializer();

    public CacheManagerConfiguration(ObjectProvider<List<AbstractCache>> cacheProvider) {
        this.caches = ListUtils.emptyIfNull(cacheProvider.getIfAvailable());
    }

    @ConditionalOnMissingBean
    @Bean(name = "cacheManager")
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {

        List<CacheManager> cacheManagers = new LinkedList<>();
        RedisCacheManager redisCacheManager = redisCacheManager(redisConnectionFactory);
        if(redisCacheManager != null) {
            cacheManagers.add(redisCacheManager);
        }
        cacheManagers.add(new ConcurrentMapCacheManager());

        CompositeCacheManager compositeCacheManager = new CompositeCacheManager();
        compositeCacheManager.setCacheManagers(cacheManagers);
        return compositeCacheManager;
    }

    public RedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {

        if(caches.isEmpty()) {
            log.warn("没有找到缓存模块");
            return null;
        }

        RedisCacheConfiguration defaultRedisCacheConfiguration =  RedisCacheConfiguration.defaultCacheConfig();
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory);

        Map<String, RedisCacheConfiguration> cacheConfigurationMap = new HashMap<>();
        Map<String, RedisSerializer> cacheSerializerMap = new HashMap<>();

        for(AbstractCache abstractCache : caches) {

            RedisSerializer redisSerializer = decideValueSerializer(abstractCache);

            RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                    .prefixKeysWith(abstractCache.getFullKeyPrefixWithColon())
                    .entryTtl(Duration.ofSeconds(abstractCache.getExpiration()))
                    .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(DEFAULT_KEY_SERIALIZER))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer))
                    .disableCachingNullValues();

            cacheConfigurationMap.put(abstractCache.getName(), redisCacheConfiguration);
            cacheSerializerMap.put(abstractCache.getName(), redisSerializer);
        }

        RedisCacheManager redisCacheManager = new RedisCacheManager(redisCacheWriter,defaultRedisCacheConfiguration, cacheConfigurationMap, false);
        redisCacheManager.setTransactionAware(true);
        redisCacheManager.afterPropertiesSet();

        caches.forEach(abstractCache -> {

            RedisTemplate redisTemplate = new RedisTemplate();
            redisTemplate.setConnectionFactory(redisConnectionFactory);
            redisTemplate.setKeySerializer(DEFAULT_KEY_SERIALIZER);
            redisTemplate.setHashKeySerializer(DEFAULT_KEY_SERIALIZER);
            redisTemplate.setValueSerializer(cacheSerializerMap.get(abstractCache.getName()));
            redisTemplate.setHashValueSerializer(cacheSerializerMap.get(abstractCache.getName()));
            redisTemplate.afterPropertiesSet();

            abstractCache.setRedisCache(redisCacheManager.getCache(abstractCache.getName()));
            abstractCache.setRedisTemplate(redisTemplate);
        });

        cacheConfigurationMap.clear();
        cacheSerializerMap.clear();

        return redisCacheManager;
    }

    private RedisSerializer decideValueSerializer(AbstractCache abstractCache) {

        Type type = abstractCache.getClass().getGenericSuperclass();
        if (!(type instanceof ParameterizedType)) {
            log.warn("{}未指定泛型参数 使用<STRING_REDIS_TEMPLATE>", abstractCache.getName());
            return jdkSerializationRedisSerializer;
        }

        Type[] params = ((ParameterizedType) type).getActualTypeArguments();
        if (params == null || params.length == 0) {
            log.warn("{}无法获取泛型参数 使用<STRING_REDIS_TEMPLATE>", abstractCache.getName());
            return jdkSerializationRedisSerializer;
        }

        Type param = params[0];
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = null;
        if (param instanceof ParameterizedType) {

            Type rawType = ((ParameterizedType) param).getRawType();

            if (Collection.class.isAssignableFrom((Class) rawType)) {
                Type[] elementType = ((ParameterizedType) param).getActualTypeArguments();
                JavaType javaType = TypeFactory.defaultInstance()
                        .constructParametricType((Class) rawType, (Class) elementType[0]);
                jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(javaType);
                jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
            } else {
                jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
                jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
            }
        } else {
            jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer((Class) param);
            jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        }
        return jackson2JsonRedisSerializer;
    }
}
