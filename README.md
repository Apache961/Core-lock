# Core-lock
基于Redis的可重入分布式锁

# 简介
分布式锁是控制分布式系统之间同步访问共享资源的一种方式。在分布式系统中，常常需要协调他们的动作。如果不同的系统或是同一个系统的不同主机之间共享了一个或一组资源，那么访问这些资源的时候，往往需要互斥来防止彼此干扰来保证一致性，在这种情况下，便需要使用到分布式锁。

# 为什么需要分布式锁
为了保证一个方法在高并发情况下的同一时间只能被同一个线程执行，在传统单体应用单机部署的情况下，可以使用Java并发处理相关的API(如ReentrantLcok或synchronized)进行互斥控制。但是，随着业务发展的需要，原单体单机部署的系统被演化成分布式系统后，由于分布式系统多线程、多进程并且分布在不同机器上，这将使原单机部署情况下的并发控制锁策略失效，为了解决这个问题就需要一种跨JVM的互斥机制来控制共享资源的访问，这就是分布式锁要解决的问题。

# 分布式锁的三种实现方式
1. 基于数据库实现分布式锁；

2. 基于缓存（Redis等）实现分布式锁；

3. 基于Zookeeper实现分布式锁；

# 什么是可重入锁
可重入就是说某个线程已经获得某个锁，可以再次获取锁而不会出现死锁。

# 使用步骤
第一步：本地maven从私服上获取依赖文件。
```xml
<dependency>
	<groupId>cn.99xf</groupId>
	<artifactId>Core-lock</artifactId>
	<version>1.0-SNAPSHOT</version>
</dependency>
```
第二步：在Spring中注册对应的锁实例，生成锁实例需要RedisTemplate。
```java
@Bean
public DistributeLock distributeLock(RedisTemplate redisTemplate) {
	DistributeLock instance = RedisDistributeLock.getInstance();
	RedisDistributeLock.setRedisTemplate(redisTemplate);
	return instance;
}
```
第三步-1：代码中注入锁实例，并使用
```java

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    DistributeLock distributeLock;

    @PostMapping("/testLock4")
    public Object testLock4(@RequestParam("lockKey") String lockKey, @RequestParam("requestID") String requestID, @RequestParam("expireTime") int expireTime) {
        System.out.println("=====[testLock4]=====");
        System.out.println("lockKey:" + lockKey);
        System.out.println("requestID:" + requestID);
        System.out.println("expireTime:" + expireTime);
        String requestId = distributeLock.lock(lockKey, requestID, expireTime);
        return requestId == null ? "kong" : requestId;
    }

    @PostMapping("/testTryAndRetry7")
    public Object testTryAndRetry7(@RequestParam("lockKey") String lockKey, @RequestParam("requestID") String requestID, @RequestParam("expireTime") int expireTime, @RequestParam("retryCount") int retryCount, @RequestParam("retryTime") int retryTime) {
        System.out.println("=====[testTryAndRetry7]=====");
        System.out.println("lockKey:" + lockKey);
        System.out.println("requestID:" + requestID);
        System.out.println("expireTime:" + expireTime);
        System.out.println("retryCount:" + retryCount);
        System.out.println("retryTime:" + retryTime);
        String requestId = distributeLock.lockAndRetry(lockKey, requestID, expireTime, retryCount, retryTime);
        return requestId == null ? "kong" : requestId;
    }

    @PostMapping("/testUnLock")
    public Object testUnLock(@RequestParam("lockKey") String lockKey, @RequestParam("requestID") String requestID) {
        System.out.println("=====[testTryAndRetry7]=====");
        System.out.println("lockKey:" + lockKey);
        System.out.println("requestID:" + requestID);
        boolean result = distributeLock.unLock(lockKey, requestID);
        return result;
    }
}
```

第三步-2：也可以通过注解的方式来使用
```java
@PostMapping("/testAnnotation1")
    @Lock(lockKey = "123")
    public Object testAnnotation1() {
        System.out.println("=====[testAnnotation1]=====");
        boolean result = true;
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @PostMapping("/testAnnotation2")
    @Lock(lockKey = "123",retryCount = -1)
    public Object testAnnotation2() {
        System.out.println("=====[testAnnotation2]=====");
        boolean result = true;
        try {
//            TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
```

# 默认值
1. 默认失效时间一分钟
2. 默认重试间隔时间300毫秒
3. 默认重试次数10次

# API说明
1. **public String lock(String lockKey)**  
	参数1：**lockKey** 锁的key  
	返回值：成功返回**requestId**，失败返回**null**  
	
2. **public String lock(String lockKey, int expireTime)**  
	参数1：**lockKey** 锁的key  
	参数2：**expireTime** 过期时间 单位：毫秒  
	返回值：成功返回**requestId**，失败返回**null**  
	
3. **public String lock(String lockKey, String requestID)**  
	参数1：**lockKey** 锁的key  
	参数2：**requestID** 上次加锁返回的requestId，重入  
	返回值：成功返回**requestId**，失败返回**null**  
	
4. **public String lock(String lockKey, String requestID, int expireTime)**  
	参数1：**lockKey** 锁的key  
	参数2：**requestID** 上次加锁返回的requestId，重入  
	参数3：**expireTime** 过期时间 单位：毫秒  
	返回值：成功返回**requestId**，失败返回**null**  
	
5. **public String lockAndRetry(String lockKey)**  
	参数1：**lockKey** 锁的key  
	返回值：成功返回**requestId**，失败返回**null**  
	
6. **public String lockAndRetry(String lockKey, String requestID)**  
	参数1：**lockKey** 锁的key  
	参数2：**requestID** 上次加锁返回的requestId，重入  
	返回值：成功返回**requestId**，失败返回**null**  
	
7. **public String lockAndRetry(String lockKey, int expireTime)**  
	参数1：**lockKey** 锁的key  
	参数2：**expireTime** 过期时间 单位：毫秒  
	返回值：成功返回**requestId**，失败返回**null**  
	
8. **public String lockAndRetry(String lockKey, int expireTime, int retryCount)**  
	参数1：**lockKey** 锁的key  
	参数2：**expireTime** 过期时间 单位：毫秒  
	参数3：**retryCount** 重试次数  
	返回值：成功返回**requestId**，失败返回**null**  
	
9. **public String lockAndRetry(String lockKey, String requestID, int expireTime)**  
	参数1：**lockKey** 锁的key  
	参数2：**requestID** 上次加锁返回的requestId，重入  
	参数3：**expireTime** 过期时间 单位：毫秒  
	返回值：成功返回**requestId**，失败返回**null**  
	
10. **public String lockAndRetry(String lockKey, String requestID, int expireTime, int retryCount)**  
	参数1：**lockKey** 锁的key  
	参数2：**requestID** 上次加锁返回的requestId，重入  
	参数3：**expireTime** 过期时间 单位：毫秒  
	参数4：**retryCount** 重试次数  
	返回值：成功返回**requestId**，失败返回**null**  
	
11. **public String lockAndRetry(String lockKey, String requestID, int expireTime, int retryCount, int retryTime)**  
	参数1：**lockKey** 锁的key  
	参数2：**requestID** 上次加锁返回的requestId，重入  
	参数3：**expireTime** 过期时间 单位：毫秒  
	参数4：**retryCount** 重试次数  
	参数5：**retryTime** 重试时间 单位：毫秒  
	返回值：成功返回**requestId**，失败返回**null**  
	
12.  **public boolean unLock(String lockKey, String requestID)**  
	参数1：**lockKey** 锁的key  
	参数2：**requestID** 上次加锁返回的requestId，重入  
	返回值：成功返回**true**，失败返回**false**  


