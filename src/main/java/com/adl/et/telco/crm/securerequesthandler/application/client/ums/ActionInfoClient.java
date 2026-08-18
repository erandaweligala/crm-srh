package com.adl.et.telco.crm.securerequesthandler.application.client.ums;

import com.adl.et.telco.crm.securerequesthandler.application.client.BaseClient;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.ExceptionHandler;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.DisplayResultCodeEnum;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.action.RouteToActionDto;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonSouthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.external.repository.cache.RoutingCacheRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import static com.adl.et.telco.crm.securerequesthandler.application.util.constants.Constants.REDIS_KEY_ROUTING;

@Component
@Slf4j
public class ActionInfoClient extends BaseClient {

    @Value("${ums.get-route-to-action}")
    private String routeToActionUrl;

    private final ExceptionHandler exceptionHandler;
    private final RestTemplate restTemplate;
    private final RoutingCacheRepository routingCacheRepository;

    private static final String SLASH = "/";
    private static final String LOG_PREFIX = "SRH|ActionInfoClient|";

    public ActionInfoClient(ExceptionHandler exceptionHandler, RestTemplate restTemplate, RoutingCacheRepository routingCacheRepository) {
        this.exceptionHandler = exceptionHandler;
        this.restTemplate = restTemplate;
        this.routingCacheRepository = routingCacheRepository;
    }

    public CommonSouthBoundResponse<List<RouteToActionDto>> getActionsByRouteUri(String routeUri) {
        try {
            String result = extractUri(routeUri);

            URI uri = UriComponentsBuilder
                    .fromHttpUrl(routeToActionUrl)
                    .queryParam("routeUri", result)
                    .build()
                    .encode()
                    .toUri();

            ResponseEntity<CommonSouthBoundResponse<List<RouteToActionDto>>> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    populateRequestEntity(),
                    new ParameterizedTypeReference<>() {}
            );

            return response.getBody();
        } catch (Exception ex) {
            throw exceptionHandler.clientExceptionHandler(ex, DisplayResultCodeEnum.GET_USER_STATUS_META_DATA_FAILED);
        }
    }

    private String extractUri(String uri) {
        log.debug("{}extractUri|START|Extracting URI from: {}", LOG_PREFIX, uri);
        String microServiceUrl = routingCacheRepository.findByKey(REDIS_KEY_ROUTING, uri);

        if (Objects.isNull(microServiceUrl)) {
            log.debug("{}findAndReturnURL|INFO|No direct match found, trying with path parameters", LOG_PREFIX);
            String result = uri.substring(0, uri.lastIndexOf(SLASH));
            return result;
        }
        return uri;
    }


}
