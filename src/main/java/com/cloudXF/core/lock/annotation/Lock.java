package com.cloudXF.core.lock.annotation;

import com.cloudXF.core.lock.service.impl.RedisDistributeLock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName: Lock
 * @Description: 分布式锁注解
 * @Author: MaoWei
 * @Date: 2020/6/10 14:44
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Lock {
    /**
     * 锁key
     * @return
     */
    String lockKey();
    /**
     * 过期时间
     * @return
     */
    int expireTime() default RedisDistributeLock.DEFAULT_EXPIRE_TIME;
    /**
     * 重试次数
     * @return
     */
    int retryCount() default RedisDistributeLock.DEFAULT_RETRY_COUNT;
    /**
     * 重试时间间隔
     * @return
     */
    int retryTime() default RedisDistributeLock.DEFAULT_RETRY_TIME;
}
