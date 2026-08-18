package com.adl.et.telco.crm.securerequesthandler.external.repository.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public class RoutingCacheRepository implements RoutingRedisRepository {

    @Value("${jwt.idle.time.range.sec}")
    private long timeLimitAC;

    @Value("${prefix.ac}")
    private String prefixAC;

    @Autowired
    private final RedisTemplate<String, String> redisTemplate;

    public RoutingCacheRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(String key,String hashKey, String value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    @Override
    public String findByKey(String key,String hashKey) {

      return Optional.ofNullable(redisTemplate.opsForHash().get(key, hashKey))
                .map(Object::toString)
                .orElse(null);
       // return redisTemplate.opsForHash().get(key, hashKey).toString();
    }


}
