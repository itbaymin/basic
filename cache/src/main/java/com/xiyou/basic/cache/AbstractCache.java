package com.xiyou.basic.cache;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Created by baiyc
 * 2020/1/19/019 17:31
 * Description：缓存抽象
 */
@Slf4j
public abstract class AbstractCache<T> implements Cache, ICacheMetadata, InitializingBean {
    /**
     * 半小时过期时间
     */
    public static final long DEFAULT_EXPIRATION_SECONDS = 1800;

    @Setter
    @Value("${spring.application.name:app}")
    private String appName;

    @Setter
    @Value("${portal.cache.deptPrefix:xiyou}")
    private String deptPrefix;

    /**
     * 完整的Key前缀. deptPrefix:appName:moduleKeyPrefix
     */
    @Getter
    private String fullKeyPrefix;
    /**
     * 带冒号
     */
    @Getter
    private String fullKeyPrefixWithColon;

    /**
     * 缓存过期时间, 秒。 (expiration < 0 表示永不过期, expiration = 0 标识使用默认过期时间, expiration > 0 使用自定义过期时间)
     */
    @Setter
    @Getter
    protected long expiration;

    @Autowired
    private Lock lock;

    @Setter
    private Cache redisCache;

    @Setter
    protected RedisTemplate redisTemplate;

    public AbstractCache() {
        this(DEFAULT_EXPIRATION_SECONDS);
    }

    public AbstractCache(long expiration) {
        this.expiration = expiration;
    }

    @Override
    public Object getNativeCache() {
        return redisTemplate;
    }

    @Override
    public ValueWrapper get(Object key) {
        return redisCache.get(key.toString());
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        return redisCache.get(key.toString(), type);
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return redisCache.get(key.toString(), valueLoader);
    }

    @Override
    public void put(Object key, Object value) {
        redisCache.put(key.toString(), value);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        return redisCache.putIfAbsent(key.toString(), value);
    }

    @Override
    public void evict(Object key) {
        redisCache.evict(key.toString());
    }

    @Override
    public void clear() {
        redisCache.clear();
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        validate();

        if (expiration == 0) {
            expiration = DEFAULT_EXPIRATION_SECONDS;
        } else {
            expiration = Math.max(expiration, -1);
        }

        this.fullKeyPrefix =
                deptPrefix.concat(":").concat(appName).concat(":").concat(moduleKeyPrefix());
        this.fullKeyPrefixWithColon = fullKeyPrefix.concat(":");
    }

    /**
     * 是否存在指定Key
     *
     * @param key
     * @return
     */
    public boolean exist(String key) {
        return BooleanUtils
                .toBoolean(redisTemplate.hasKey(fullKeyPrefixWithColon.concat(key)));
    }

    /**
     * 获取当前缓存模块的缓存数
     *
     * @return
     */
    public long size() {
        return redisTemplate.keys("*" + fullKeyPrefix + "*").size();
    }

    /**
     * 获取指定Key在Redis缓存中的失效时间(秒)
     *
     * @param key
     * @return
     */
    public long expireSecond(String key) {
        Long expire = redisTemplate
                .getExpire(fullKeyPrefixWithColon.concat(key), TimeUnit.SECONDS);
        return expire == null ? 0l : expire.longValue();
    }

    protected String key(String key) {
        return fullKeyPrefixWithColon.concat(key);
    }

    private void validate() {
        if (StringUtils.isEmpty(getName())) {
            throw new IllegalArgumentException("需要提供缓存块名称");
        }

        if (StringUtils.isEmpty(moduleKeyPrefix())) {
            throw new IllegalArgumentException("需要提供缓存Key的业务模块前缀");
        }
    }

    public boolean lock(String key) {
        return lock.lockWait(key(key));
    }

    public void unlock(String key) {
        lock.unlock(key(key));
    }
}
