package com.adl.et.telco.crm.securerequesthandler.external.repository.cache;

public interface AuthRedisRepository {

    void save(String key, String value, Long expiryTimeInSec);

    String findByKey(String key);


    void delete(String userName);

    boolean existsByUserId(String userId);



    void updateExpiryTime(String accessToken, Long timeLimitAC);


    boolean existsByKey(String key);


    void deleteKey(String s);
}
