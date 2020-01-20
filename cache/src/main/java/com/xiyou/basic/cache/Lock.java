package com.xiyou.basic.cache;

import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * Created by baiyc
 * 2020/1/19/019 17:44
 * Description：
 */
public class Lock {
    private static final Logger log = LoggerFactory.getLogger(Lock.class);
    private static final ThreadLocal<Long> threadLocal = new ThreadLocal();
    private StringRedisTemplate stringRedisTemplate;
    @Value("${lock.ttl: 3000}")
    private long lockTTL;
    @Value("${lock.wait: 3000}")
    private long lockWait;

    public Lock(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public boolean lockWait(String lockKey) {
        return this.lockWait(lockKey, this.lockWait);
    }

    public boolean lockWait(String lockKey, long lockWaitMillSec) {
        log.info("尝试获取锁{}, {}", lockKey, Long.valueOf(lockWaitMillSec));
        long start = System.currentTimeMillis();

        while(!this.lock(lockKey)) {
            try {
                long e = System.currentTimeMillis();
                if(e - start > lockWaitMillSec) {
                    log.warn("获取锁{}超时{}", lockKey, Long.valueOf(lockWaitMillSec));
                    return false;
                }

                Thread.sleep(100L);
            } catch (InterruptedException var8) {
                log.error("等待获得锁异常", var8);
            }
        }

        log.info("成功获取到锁{}", lockKey);
        return true;
    }

    boolean lock(String key) {
        return this.lock(key, this.lockTTL);
    }

    boolean lock(String key, long lockTTLMillSec) {
        String lockKey = this.lockKey(key);
        ValueOperations valueOperations = this.stringRedisTemplate.opsForValue();
        long expireAt = System.currentTimeMillis() + lockTTLMillSec;
        boolean acquired;
        if(acquired = valueOperations.setIfAbsent(lockKey, String.valueOf(expireAt)).booleanValue()) {
            threadLocal.set(Long.valueOf(expireAt));
        } else {
            String expiredTimeOldValue = (String)valueOperations.get(lockKey);
            if(expiredTimeOldValue != null && Long.valueOf(expiredTimeOldValue).longValue() < System.currentTimeMillis()) {
                String oldValue = (String)valueOperations.getAndSet(lockKey, String.valueOf(expireAt));
                if(StringUtils.equals(oldValue, expiredTimeOldValue)) {
                    log.info("解锁超时锁并重新获得锁 {}:{} => {}", new Object[]{lockKey, expiredTimeOldValue, Long.valueOf(expireAt)});
                    threadLocal.set(Long.valueOf(expireAt));
                    return true;
                }
            }
        }

        return acquired;
    }

    public void unlock(String key) {
        log.info("释放锁{}", key);
        if(threadLocal.get() != null) {
            String lockKey = this.lockKey(key);
            Object expiredTimeAt = this.stringRedisTemplate.opsForValue().get(lockKey);
            if(expiredTimeAt != null) {
                long currentTime = System.currentTimeMillis();
                if(Long.valueOf(expiredTimeAt.toString()).longValue() > currentTime && ((Long)threadLocal.get()).longValue() > currentTime) {
                    this.stringRedisTemplate.delete(lockKey);
                }
            }

            threadLocal.remove();
        }
    }

    String lockKey(String key) {
        return String.format("%s~lock", new Object[]{key});
    }
}
