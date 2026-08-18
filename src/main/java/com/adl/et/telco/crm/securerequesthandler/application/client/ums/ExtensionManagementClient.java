package com.adl.et.telco.crm.securerequesthandler.application.client.ums;

import com.adl.et.telco.crm.securerequesthandler.application.client.BaseClient;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonSouthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.RouteInfo;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.ums.extensions.CreateExtensionRequest;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.ums.extensions.ExtensionDTO;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.BaseException;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.ExceptionHandler;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.DisplayResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Objects;

/**
 * Client for managing UMS extensions and routing paths.
 */
@Component
@Slf4j
public class ExtensionManagementClient extends BaseClient {

    private final ExceptionHandler exceptionHandler;
    private final RestTemplate restTemplate;
    private final String extensionManagementUrl;
    private final String routerPathUrl;
    private final String routerPathCrmExtensionUrl;

    @Autowired
    public ExtensionManagementClient(
            ExceptionHandler exceptionHandler,
            RestTemplate restTemplate,
            @Value("${ums.extension-management.url}") String extensionManagementUrl,
            @Value("${ums.routing-path.url}") String routerPathUrl,
            @Value("${ums.routing-path-crm-extension.url}") String routerPathCrmExtensionUrl) {
        this.exceptionHandler = exceptionHandler;
        this.restTemplate = restTemplate;
        this.extensionManagementUrl = extensionManagementUrl;
        this.routerPathUrl = routerPathUrl;
        this.routerPathCrmExtensionUrl = routerPathCrmExtensionUrl;
    }

    /**
     * Retrieves the list of all extensions.
     *
     * @return CommonSouthBoundResponse containing the list of extensions
     */
    public CommonSouthBoundResponse<List<ExtensionDTO>> getExtensionsList() {
        return executeRequest(
                extensionManagementUrl,
                HttpMethod.GET,
                new ParameterizedTypeReference<CommonSouthBoundResponse<List<ExtensionDTO>>>() {},
                null,
                DisplayResultCodeEnum.GET_USER_STATUS_META_DATA_FAILED
        );
    }

    /**
     * Creates a new extension.
     *
     * @param newExtension The extension to create
     * @return CommonSouthBoundResponse containing the result
     */
    public CommonSouthBoundResponse<String> createExtension(CreateExtensionRequest newExtension) {
        return executeRequest(
                extensionManagementUrl,
                HttpMethod.POST,
                new ParameterizedTypeReference<CommonSouthBoundResponse<String>>() {},
                newExtension,
                DisplayResultCodeEnum.CREATE_USER_FAILED
        );
    }

    /**
     * Retrieves all routing paths.
     *
     * @return List of routing paths
     * @throws BaseException if the request fails
     */
    public List<RouteInfo> getAllRoutingPaths() throws BaseException {
        return getRoutingPaths(routerPathUrl);
    }

    /**
     * Retrieves all CRM extension routing paths.
     *
     * @return List of routing paths
     * @throws BaseException if the request fails
     */
    public List<RouteInfo> getAllRoutingPathsCrmExtension() throws BaseException {
        return getRoutingPaths(routerPathCrmExtensionUrl);
    }

    /**
     * Helper method to execute HTTP requests with consistent error handling.
     *
     * @param url The URL to call
     * @param method The HTTP method to use
     * @param responseType The expected response type
     * @param requestBody The request body (can be null)
     * @param errorCode The error code to use if the request fails
     * @param <T> The response type
     * @return The response
     */
    private <T> T executeRequest(
            String url,
            HttpMethod method,
            ParameterizedTypeReference<T> responseType,
            Object requestBody,
            DisplayResultCodeEnum errorCode) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
            ResponseEntity<T> response = restTemplate.exchange(
                    builder.build().toString(),
                    method,
                    requestBody != null ? populateRequestEntity(requestBody) : populateRequestEntity(),
                    responseType
            );
            return response.getBody();
        } catch (Exception ex) {
            throw exceptionHandler.clientExceptionHandler(ex, errorCode);
        }
    }

    /**
     * Helper method to get routing paths with consistent error handling.
     *
     * @param url The URL to call
     * @return List of routing paths
     * @throws BaseException if the request fails
     */
    private List<RouteInfo> getRoutingPaths(String url) throws BaseException {
        try {
            CommonSouthBoundResponse<List<RouteInfo>> response = executeRequest(
                    url,
                    HttpMethod.GET,
                    new ParameterizedTypeReference<CommonSouthBoundResponse<List<RouteInfo>>>() {},
                    null,
                    DisplayResultCodeEnum.GET_ALL_ROUTE_PATH_DETAILS_FOR_CACHE_FAILED
            );

            if (Objects.nonNull(response)) {
                return response.getResponseData();
            }
            throw new BaseException(
                    DisplayResultCodeEnum.GET_ALL_ROUTE_PATH_DETAILS_FOR_CACHE_FAILED.description(),
                    DisplayResultCodeEnum.GET_ALL_ROUTE_PATH_DETAILS_FOR_CACHE_FAILED.description(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    DisplayResultCodeEnum.GET_ALL_ROUTE_PATH_DETAILS_FOR_CACHE_FAILED.code(),
                    null
            );
        } catch (HttpStatusCodeException ex) {
            throw new BaseException(
                    ex.getMessage(),
                    ex.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE,
                    DisplayResultCodeEnum.GET_ALL_ROUTE_PATH_DETAILS_FOR_CACHE_FAILED.code(),
                    ex.getStackTrace()
            );
        }
    }
}
