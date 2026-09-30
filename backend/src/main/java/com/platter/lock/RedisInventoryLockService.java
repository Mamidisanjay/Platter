package com.platter.lock;

import java.time.Duration;
import java.util.UUID;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
public class RedisInventoryLockService implements InventoryLockService {
    private static final Duration LOCK_TTL = Duration.ofSeconds(10);
    private static final String KEY_PREFIX = "platter:lock:inventory:";
    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>("if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end", Long.class);
    private final RedisTemplate<String, String> redisTemplate;

    public RedisInventoryLockService(RedisTemplate<String, String> redisTemplate) { this.redisTemplate = redisTemplate; }

    @Override
    public String tryLock(Long menuItemId) {
        String token = UUID.randomUUID().toString();
        boolean acquired = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(KEY_PREFIX + menuItemId, token, LOCK_TTL));
        return acquired ? token : null;
    }

    @Override
    public void release(Long menuItemId, String token) {
        if (token != null) redisTemplate.execute(RELEASE_SCRIPT, java.util.List.of(KEY_PREFIX + menuItemId), token);
    }
}
