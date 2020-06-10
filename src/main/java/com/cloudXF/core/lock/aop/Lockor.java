package com.cloudXF.core.lock.aop;

import com.cloudXF.core.lock.annotation.Lock;
import com.cloudXF.core.lock.service.DistributeLock;
import com.cloudXF.core.lock.service.impl.RedisDistributeLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @ClassName: Validator
 * @Description: 自定义校验处理类
 * @Author: MaoWei
 * @Date: 2020/1/3 16:35
 **/
@Aspect
@Component
public class Lockor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Lockor.class);

    private static final ThreadLocal<String> REQUEST_ID_MAP = new ThreadLocal<>();

    @Pointcut("@annotation(com.cloudXF.core.lock.annotation.Lock)")
    public void annotationPointcut() {
    }

    @Around("annotationPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Lock annotation = method.getAnnotation(Lock.class);

        boolean lockSuccess = lock(annotation);
        if (lockSuccess) {
            Object result = joinPoint.proceed();
            unlock(annotation);
            return result;
        }
        return null;
    }

    /**
     * 加锁
     */
    private boolean lock(Lock annotation) {
        DistributeLock distributeLock = RedisDistributeLock.getInstance();

        int retryCount = annotation.retryCount();

        String requestID = REQUEST_ID_MAP.get();
        if (requestID != null) {
            // 当前线程 已经存在requestID
            distributeLock.lockAndRetry(annotation.lockKey(), requestID, annotation.expireTime(), retryCount);
            LOGGER.info("重入加锁成功 requestID=" + requestID);

            return true;
        } else {
            // 当前线程 不存在requestID
            String newRequestID = distributeLock.lockAndRetry(annotation.lockKey(), annotation.expireTime(), retryCount);

            if (newRequestID != null) {
                // 加锁成功，设置新的requestID
                REQUEST_ID_MAP.set(newRequestID);
                LOGGER.info("加锁成功 newRequestID=" + newRequestID);

                return true;
            } else {
                LOGGER.info("加锁失败，超过重试次数，直接返回 retryCount={}", retryCount);

                return false;
            }
        }
    }

    /**
     * 解锁
     */
    private void unlock(Lock annotation) {
        DistributeLock distributeLock = RedisDistributeLock.getInstance();
        String requestID = REQUEST_ID_MAP.get();
        if (requestID != null) {
            // 解锁成功
            boolean unLockSuccess = distributeLock.unLock(annotation.lockKey(), requestID);
            if (unLockSuccess) {
                // 移除 ThreadLocal中的数据
                REQUEST_ID_MAP.remove();
                LOGGER.info("解锁成功 requestID=" + requestID);
            }
        }
    }
}
