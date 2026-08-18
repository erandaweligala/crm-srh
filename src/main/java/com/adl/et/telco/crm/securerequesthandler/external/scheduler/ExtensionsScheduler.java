package com.adl.et.telco.crm.securerequesthandler.external.scheduler;

import com.adl.et.telco.crm.securerequesthandler.application.client.ums.ExtensionManagementClient;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.RouteInfo;
import com.adl.et.telco.crm.securerequesthandler.external.repository.cache.RoutingCacheRepository;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceinterface.AsyncAdaptorInterface;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.adl.et.telco.crm.securerequesthandler.application.util.constants.Constants.REDIS_KEY_ROUTING;
import static com.adl.et.telco.crm.securerequesthandler.application.util.constants.Constants.REDIS_ROUTING_CRM_EXTENSION;

@Service
@Slf4j
public class ExtensionsScheduler {


    private final ExtensionManagementClient extensionManagementClient;
    private final RoutingCacheRepository routingCacheRepository;
    private final AsyncAdaptorInterface asyncAdaptorInterface;
    @Autowired
    public ExtensionsScheduler(ExtensionManagementClient extensionManagementClient, RoutingCacheRepository routingCacheRepository, AsyncAdaptorInterface asyncAdaptorInterface) {
        this.extensionManagementClient = extensionManagementClient;
        this.routingCacheRepository = routingCacheRepository;
        this.asyncAdaptorInterface = asyncAdaptorInterface;
    }


    @Scheduled(fixedRate = 600000) // 10 minutes = 600,000 milliseconds
    @SneakyThrows
    public void cacheSyncExtensionsRouteDetails() {
        try {
            CompletableFuture<Object>[] completableFutures = asyncAdaptorInterface.supplyAll(100000L,
                    extensionManagementClient::getAllRoutingPaths,
                    extensionManagementClient::getAllRoutingPathsCrmExtension
            );

            List<RouteInfo> routerInfoList = (List<RouteInfo>) completableFutures[0].get();
            List<RouteInfo> routerInfoCrmExtensionList = (List<RouteInfo>) completableFutures[1].get();
            if (routerInfoList != null && !routerInfoList.isEmpty()) {
                routerInfoList
                        .forEach(routeInfo -> routingCacheRepository.save(REDIS_KEY_ROUTING, routeInfo.getInPath(), routeInfo.getOutURL()));
                log.info("SRH | Scheduler Executed in ExtensionsScheduler.cacheSyncExtensionsRouteDetails with rate {}", "600000");
            } else {
                log.error("SRH | Scheduler Failed in ExtensionsScheduler.cacheSyncExtensionsRouteDetails.IllegalArgumentException | Empty Response from UMS");
            }

            if(routerInfoCrmExtensionList != null && !routerInfoCrmExtensionList.isEmpty()){
                routerInfoCrmExtensionList
                        .forEach(routeInfo -> routingCacheRepository.save(REDIS_ROUTING_CRM_EXTENSION, routeInfo.getInPath(), routeInfo.getOutURL()));
            }
        } catch (IllegalArgumentException e) {
            log.error("SRH | Scheduler Exception in ExtensionsScheduler.cacheSyncExtensionsRouteDetails.IllegalArgumentException | {}", e.getLocalizedMessage());
        } catch (RestClientException e) {
            log.error("SRH | Scheduler Exception in ExtensionsScheduler.cacheSyncExtensionsRouteDetails.RestClientException | {}", e.getLocalizedMessage());
        }
    }

}
