package com.adl.et.telco.crm.securerequesthandler.external.repository.cache;

import java.util.Map;

public interface ActionRedisRepository {

    void saveActionDetails(Integer key, Map<Integer,String> value);
    Map<Integer,String> findByActionId(Integer key);

}
