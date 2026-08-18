package com.adl.et.telco.crm.securerequesthandler.external.repository.cache;

public interface RoutingRedisRepository {

    void save(String key,String hashKey, String value);

    String findByKey(String key,String hashKey);
}
