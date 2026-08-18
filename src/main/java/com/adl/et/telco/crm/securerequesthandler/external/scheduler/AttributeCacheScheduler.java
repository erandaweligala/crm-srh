package com.adl.et.telco.crm.securerequesthandler.external.scheduler;

import com.adl.et.telco.crm.securerequesthandler.application.client.accessclient.AtributeClient;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.authentication.AttributeDetails;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.authentication.AttributeItem;
import com.adl.et.telco.crm.securerequesthandler.external.repository.cache.ActionCacheRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AttributeCacheScheduler {

    @Autowired
    private AtributeClient atributeClient;
    @Autowired
    private ActionCacheRepository actionCacheRepository;

    @Scheduled(fixedRate = 600000) // 10 minutes = 600,000 milliseconds
    public void cacheSyncAttributeDetails() {
        try {
            List<AttributeDetails> attributesList = atributeClient.getAttributeDetails().getResponseData();
            if (attributesList != null && !attributesList.isEmpty()) {
                attributesList.stream().forEach(e -> actionCacheRepository.saveActionDetails(e.getActionId(), e.getAttributeList().stream().collect(Collectors.toMap(AttributeItem::getId, AttributeItem::getPath))));
                log.info("SRH | Scheduler Executed in AttributeCacheScheduler.cacheSyncAttributeDetails with rate {}", "600000");
            } else
                log.error("SRH | Scheduler Failed in AttributeCacheScheduler.cacheSyncAttributeDetails.IllegalArgumentException | Empty Response from UMS");

        } catch (IllegalArgumentException e) {
            log.error("SRH | Scheduler Exception in AttributeCacheScheduler.cacheSyncAttributeDetails.IllegalArgumentException | {}", e.getLocalizedMessage());
        } catch (RestClientException e) {
            log.error("SRH | Scheduler Exception in AttributeCacheScheduler.cacheSyncAttributeDetails.RestClientException | {}", e.getLocalizedMessage());
        }
    }
}
