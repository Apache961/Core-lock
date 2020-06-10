package com.cloudXF.core.lock.service.impl;

import com.cloudXF.core.lock.helper.LuaScript;
import com.cloudXF.core.lock.service.DistributeLock;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @ClassName: RedisDistributeLock
 * @Description: Redis分布式锁实现
 * @Author: MaoWei
 * @Date: 2020/6/10 9:15
 **/
public final class RedisDistributeLock implements DistributeLock {

    // ==========================================常量定义================================================
    /**
     * 无限重试
     */
    public static final int UN_LIMIT_RETRY = -1;
    /**
     * 操作成功标识
     */
    private static final Long OP_SUCCESS = 1L;
    /**
     * 默认过期时间 单位：毫秒
     */
    public static final int DEFAULT_EXPIRE_TIME = 60 * 1000;
    /**
     * 默认加锁重试时间 单位：毫秒
     */
    public static final int DEFAULT_RETRY_TIME = 300;
    /**
     * 默认的加锁重试次数
     */
    public static final int DEFAULT_RETRY_COUNT = 10;

    // ==========================================初始化================================================

    /**
     * 单例锁实例
     */
    private static DistributeLock instance = new RedisDistributeLock();

    private static RedisTemplate redisTemplate;

    public static void setRedisTemplate(RedisTemplate redisTemplate) {
        RedisDistributeLock.redisTemplate = redisTemplate;
    }

    /**
     * 在构造器中初始化Lua脚本
     */
    private RedisDistributeLock() {
        LuaScript.init();
    }

    /**
     * 获取单例锁实例
     *
     * @return
     */
    public static DistributeLock getInstance() {
        return instance;
    }

    // ==========================================加锁================================================

    /**
     * 尝试加锁
     *
     * @param lockKey 锁的key
     * @return 加锁成功 返回uuid
     * 加锁失败 返回null
     */
    @Override
    public String lock(String lockKey) {
        String uuid = UUID.randomUUID().toString();
        return lock(lockKey, uuid);
    }

    /**
     * 尝试加锁 (requestID相等 可重入)
     *
     * @param lockKey    锁的key
     * @param expireTime 过期时间 单位：毫秒
     * @return 加锁成功 返回uuid
     * 加锁失败 返回null
     */
    @Override
    public String lock(String lockKey, int expireTime) {
        String uuid = UUID.randomUUID().toString();
        return lock(lockKey, uuid, expireTime);
    }

    /**
     * 尝试加锁 (requestID相等 可重入)
     *
     * @param lockKey   锁的key
     * @param requestID 用户ID
     * @return 加锁成功 返回uuid
     * 加锁失败 返回null
     */
    @Override
    public String lock(String lockKey, String requestID) {
        return lock(lockKey, requestID, DEFAULT_EXPIRE_TIME);
    }

    /**
     * 尝试加锁 (requestID相等 可重入)
     *
     * @param lockKey    锁的key
     * @param requestID  用户ID
     * @param expireTime 过期时间 单位：毫秒
     * @return 加锁成功 返回uuid
     * 加锁失败 返回null
     */
    @Override
    public String lock(String lockKey, String requestID, int expireTime) {

        List<String> keyList = Arrays.asList(lockKey);

        Long execute = (Long) redisTemplate.execute(LuaScript.getLockScript(), keyList, requestID, expireTime);

        if (OP_SUCCESS.equals(execute)) {
            return requestID;
        } else {
            return null;
        }
    }

    // ==========================================加锁重试================================================

    /**
     * 尝试加锁，失败自动重试 会阻塞当前线程
     *
     * @param lockKey 锁的key
     * @return 加锁成功 返回uuid
     * 加锁失败 返回null
     */
    @Override
    public String lockAndRetry(String lockKey) {
        String uuid = UUID.randomUUID().toString();
        return lockAndRetry(lockKey, uuid);
    }

    /**
     * 尝试加锁，失败自动重试 会阻塞当前线程 (requestID相等 可重入)
     *
     * @param lockKey   锁的key
     * @param requestID 用户ID
     * @return 加锁成功 返回uuid
     * 加锁失败 返回null
     */
    @Override
    public String lockAndRetry(String lockKey, String requestID) {
        return lockAndRetry(lockKey, requestID, DEFAULT_EXPIRE_TIME);
    }

    /**
     * 尝试加锁 (requestID相等 可重入)
     *
     * @param lockKey    锁的key
     * @param expireTime 过期时间 单位：毫秒
     * @return 加锁成功 返回uuid
     * 加锁失败 返回null
     */
    @Override
    public String lockAndRetry(String lockKey, int expireTime) {
        String uuid = UUID.randomUUID().toString();
        return lockAndRetry(lockKey, uuid, expireTime);
    }

    /**
     * 尝试加锁 (requestID相等 可重入)
     *
     * @param lockKey    锁的key
     * @param expireTime 过期时间 单位：毫秒
     * @param retryCount 重试次数
     * @return 加锁成功 返回uuid
     * 加锁失败 返回null
     */
    @Override
    public String lockAndRetry(String lockKey, int expireTime, int retryCount) {
        String uuid = UUID.randomUUID().toString();
        return lockAndRetry(lockKey, uuid, expireTime, retryCount);
    }

    /**
     * 尝试加锁 (requestID相等 可重入)
     *
     * @param lockKey    锁的key
     * @param requestID  用户ID
     * @param expireTime 过期时间 单位：毫秒
     * @return 加锁成功 返回uuid
     * 加锁失败 返回null
     */
    @Override
    public String lockAndRetry(String lockKey, String requestID, int expireTime) {
        return lockAndRetry(lockKey, requestID, expireTime, DEFAULT_RETRY_COUNT);
    }

    /**
     * 尝试加锁 (requestID相等 可重入)
     *
     * @param lockKey    锁的key
     * @param expireTime 过期时间 单位：毫秒
     * @param requestID  用户ID
     * @param retryCount 重试次数
     * @return 加锁成功 返回uuid
     * 加锁失败 返回null
     */
    @Override
    public String lockAndRetry(String lockKey, String requestID, int expireTime, int retryCount) {
        return lockAndRetry(lockKey, requestID, expireTime, retryCount, DEFAULT_RETRY_TIME);
    }

    /**
     * 尝试加锁 (requestID相等 可重入)
     *
     * @param lockKey    锁的key
     * @param expireTime 过期时间 单位：毫秒
     * @param requestID  用户ID
     * @param retryCount 重试次数
     * @param retryTime  重试时间 单位：毫秒
     * @return 加锁成功 返回uuid
     * 加锁失败 返回null
     */
    @Override
    public String lockAndRetry(String lockKey, String requestID, int expireTime, int retryCount, int retryTime) {
        if (retryCount <= 0) {
            // retryCount小于等于0 无限循环，一直尝试加锁
            while (true) {
                String result = lock(lockKey, requestID, expireTime);
                if (result != null) {
                    return result;
                }
                try {
                    // 休眠一会
                    Thread.sleep(retryTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            // retryCount大于0 尝试指定次数后，退出
            for (int i = 0; i < retryCount; i++) {
                String result = lock(lockKey, requestID, expireTime);
                if (result != null) {
                    return result;
                }
                try {
                    // 休眠一会
                    Thread.sleep(retryTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    // ==========================================解锁================================================

    /**
     * 释放锁
     *
     * @param lockKey   锁的key
     * @param requestID 用户ID
     * @return true     释放自己所持有的锁 成功
     * false    释放自己所持有的锁 失败
     */
    @Override
    public boolean unLock(String lockKey, String requestID) {

        List<String> keyList = Arrays.asList(lockKey);

        Long execute = (Long) redisTemplate.execute(LuaScript.getUnLockScript(), keyList, requestID);
        // 释放锁成功
        return OP_SUCCESS.equals(execute);
    }
}
