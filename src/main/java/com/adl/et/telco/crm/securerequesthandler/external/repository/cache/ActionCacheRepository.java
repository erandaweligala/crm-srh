package com.adl.et.telco.crm.securerequesthandler.external.repository.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class ActionCacheRepository implements ActionRedisRepository {

    private String prefix = "CRM_TELCO_DEV_ACTION_";

    private String tenant = "1";

    @Autowired
    private final RedisTemplate<String, Map<Integer, String>> redisTemplate;

    public ActionCacheRepository(RedisTemplate<String, Map<Integer, String>> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void saveActionDetails(Integer actionId, Map<Integer, String> value) {
        redisTemplate.opsForValue().set(prefix + tenant + "_" + actionId, value);
    }

    @Override
    public Map<Integer, String> findByActionId(Integer key) {
        return redisTemplate.opsForValue().get(prefix + tenant + "_" + key);
    }


}
