package com.adl.et.telco.crm.securerequesthandler.external.repository.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

/**
 * Repository implementation for handling authentication-related cache operations using Redis.
 * Provides methods for saving, retrieving, and managing authentication tokens in the cache.
 */
@Repository
public class AuthCacheRepository implements AuthRedisRepository {
    
    @Value("${prefix.ac}")
    private String prefixAC;

    private static final String KEY_MUST_NOT_BE_NULL = "Key must not be null";

    private final RedisTemplate<String, String> redisTemplate;

    public AuthCacheRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(String key, String value, Long expiryTimeInSec) {
        if (key == null || value == null || expiryTimeInSec == null) {
            throw new IllegalArgumentException("Key, value, and expiry time must not be null");
        }
        redisTemplate.opsForValue().set(key, value);
        redisTemplate.expire(key, expiryTimeInSec, TimeUnit.SECONDS);
    }

    @Override
    public String findByKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException(KEY_MUST_NOT_BE_NULL);
        }
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void delete(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }
        redisTemplate.opsForValue().getOperations().delete(prefixAC + userId);
    }

    @Override
    public boolean existsByUserId(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }
        Boolean hasKey = redisTemplate.opsForValue().getOperations().hasKey(prefixAC + userId);
        return Boolean.TRUE.equals(hasKey);
    }

    @Override
    public void updateExpiryTime(String key, Long expiryTimeInSec) {
        if (key == null || expiryTimeInSec == null) {
            throw new IllegalArgumentException("Key and expiry time must not be null");
        }
        redisTemplate.expire(key, expiryTimeInSec, TimeUnit.SECONDS);
    }

    @Override
    public boolean existsByKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException(KEY_MUST_NOT_BE_NULL);
        }
        Boolean hasKey = redisTemplate.opsForValue().getOperations().hasKey(key);
        return Boolean.TRUE.equals(hasKey);
    }

    @Override
    public void deleteKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException(KEY_MUST_NOT_BE_NULL);
        }
        redisTemplate.opsForValue().getOperations().delete(key);
    }
}
