package com.cloudXF.core.lock.helper;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * @ClassName: LuaScriptHelper
 * @Description: Lua脚本帮助类
 * @Author: MaoWei
 * @Date: 2020/6/10 9:21
 **/
public class LuaScript {

    /**
     * 加锁脚本 lock.lua
     */
    private static RedisScript<String> LOCK_SCRIPT;

    /**
     * 解锁脚本 unlock.lua
     */
    private static RedisScript<String> UN_LOCK_SCRIPT;

    public static void init() {
        initLockScript();
        initUnLockScript();
    }

    private static void initLockScript() {
        DefaultRedisScript<String> defaultRedisScript = new DefaultRedisScript<>();
        defaultRedisScript.setResultType(String.class);
        defaultRedisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lock.lua")));
        LOCK_SCRIPT = defaultRedisScript;
    }

    private static void initUnLockScript() {
        DefaultRedisScript<String> defaultRedisScript = new DefaultRedisScript<>();
        defaultRedisScript.setResultType(String.class);
        defaultRedisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("unlock.lua")));
        UN_LOCK_SCRIPT = defaultRedisScript;
    }

    public static RedisScript<String> getLockScript() {
        return LOCK_SCRIPT;
    }

    public static RedisScript<String> getUnLockScript() {
        return UN_LOCK_SCRIPT;
    }
}
