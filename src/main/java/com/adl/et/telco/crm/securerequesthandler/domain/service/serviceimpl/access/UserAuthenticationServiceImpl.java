/**
 * Copyrights 2020 Axiata Digital Labs Pvt Ltd.
 * All Rights Reserved.
 *
 * These materials are unpublished, proprietary, confidential source
 * code of Axiata Digital Labs Pvt Ltd (ADL) and constitute a TRADE
 * SECRET of ADL.
 *
 * ADL retains all title to and intellectual property rights in these
 * materials.
 */
package com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.access;

import com.adl.et.telco.crm.securerequesthandler.application.client.accessclient.CpqUserDetailClient;
import com.adl.et.telco.crm.securerequesthandler.application.client.accessclient.CrmUserDetailClient;
import com.adl.et.telco.crm.securerequesthandler.application.client.ums.CustomPropertiesClient;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.authentication.AccessSystemDTO;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.authentication.PermissionDTO;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.authentication.UserBasicInfo;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.authentication.UserDetailsForToken;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonNorthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonSouthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.security.AccessTokenResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.security.TokenEnhancementRequest;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.security.UserLoginRequest;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.ums.customproperties.CustomPropertyDTO;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.ums.groups.UserGroupDto;
import com.adl.et.telco.crm.securerequesthandler.external.repository.cache.AuthCacheRepository;
import com.adl.et.telco.crm.securerequesthandler.application.util.ResponseHandler;
import com.adl.et.telco.crm.securerequesthandler.application.util.constants.ServiceConstants;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.BaseException;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.AuthCodeEnum;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.adl.et.telco.crm.securerequesthandler.application.util.constants.Constants.EMAIL_REGEX;

@Service
@Slf4j
public class UserAuthenticationServiceImpl implements UserAuthenticationService {
    private static final String LOG_PREFIX = "SRH|UserAuthenticationService|";
    private static final String START = "START";
    private static final String ERROR = "ERROR";
    private static final String SUCCESS = "SUCCESS";

    private final JwtService jwtService;
    private final AuthCacheRepository cacheRepository;
    private final CrmUserDetailClient crmUserDetailClient;
    private final CustomPropertiesClient customPropertiesClient;
    private final ResponseHandler responseHandler;
    private final long refreshTimeRange;
    private final long timeLimitAC;
    private final String prefixAC;
    private final String tempTokenPrefix;

    public UserAuthenticationServiceImpl(
            JwtService jwtService,
            AuthCacheRepository cacheRepository,
            CrmUserDetailClient crmUserDetailClient,
            CustomPropertiesClient customPropertiesClient,
            ResponseHandler responseHandler,
            @Value("${jwt.refresh.time.range.sec}") long refreshTimeRange,
            @Value("${jwt.idle.time.range.sec}") long timeLimitAC,
            @Value("${prefix.ac}") String prefixAC,
            @Value("${prefix.temp}") String tempTokenPrefix) {
        this.jwtService = jwtService;
        this.cacheRepository = cacheRepository;
        this.crmUserDetailClient = crmUserDetailClient;
        this.customPropertiesClient = customPropertiesClient;
        this.responseHandler = responseHandler;
        this.refreshTimeRange = refreshTimeRange;
        this.timeLimitAC = timeLimitAC;
        this.prefixAC = prefixAC;
        this.tempTokenPrefix = tempTokenPrefix;
    }

    @Override
    public CommonNorthBoundResponse<AccessTokenResponse> login(String requestVerificationToken, UserLoginRequest userLoginRequest,
                                                               String productId) throws BaseException {
        log.info("{}|{}|Login attempt for productId: {}, tenant: {}", LOG_PREFIX, START, productId, userLoginRequest.getTenant());
        try {
            String userName = crmUserDetailClient.getUserName(userLoginRequest).getResponseData();
           // String userName = "deshala.mendis";
            userName = extractUsername(userName);

            // extract user id from user name
            String userId = extractUserId(userName);

            log.debug("{}|Extracted userId: {} | {}", LOG_PREFIX, userId, userName);

            // verify if the userId have access to the product
            if (!isUserHaveAccessToSystem(userId, productId)) {
                log.error("{}|User does not have access to the system: {}", LOG_PREFIX, userName);
                throw new BaseException(
                    AuthCodeEnum.USER_NOT_HAVE_ACCESS_TO_SYSTEM.description(),
                    AuthCodeEnum.USER_NOT_HAVE_ACCESS_TO_SYSTEM.description(),
                    HttpStatus.FORBIDDEN,
                    AuthCodeEnum.USER_NOT_HAVE_ACCESS_TO_SYSTEM.code(),
                    null
                );
            }

            Map<String, String> accessTokenMap = createAccessToken(userName, userLoginRequest.getTenant(), productId);
            cacheRepository.deleteKey(tempTokenPrefix + userName);

            String newRequestVerificationToken = UUID.randomUUID().toString();
            cacheRepository.save(prefixAC + userName, newRequestVerificationToken, timeLimitAC);

            log.info("{}|{}|Login successful for userId: {}", LOG_PREFIX, SUCCESS, userName);
            return responseHandler.responseBuilder(
                new AccessTokenResponse(accessTokenMap.get(ServiceConstants.TOKEN), newRequestVerificationToken),
                AuthCodeEnum.LOGIN_SUCCESS.description(),
                AuthCodeEnum.LOGIN_SUCCESS.code()
            );
        } catch (BaseException ex) {
            log.error("{}|{}|BaseException during login: {}", LOG_PREFIX, ERROR, ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("{}|{}|Unexpected error during login: {}", LOG_PREFIX, ERROR, ex.getMessage(), ex);
            throw new BaseException(
                ex.getMessage(),
                AuthCodeEnum.LOGIN_INTERNAL_SERVER_ERROR.description(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                AuthCodeEnum.LOGIN_INTERNAL_SERVER_ERROR.code(),
                ex.getStackTrace()
            );
        }
    }

    private String extractUserId(String userName) {

        try {
            CommonSouthBoundResponse<UserBasicInfo> infoByUserId = crmUserDetailClient.getUserDetailByUserName(userName);

            if (infoByUserId.getResponseData() != null) {
                UserBasicInfo userDetails = infoByUserId.getResponseData();
                String userId = userDetails.getUserId();
                log.debug("{}|{}|Extracted userId: {}", LOG_PREFIX, SUCCESS, userId);
                return String.valueOf(userId);
            } else {
                log.error("{}|User details not found for userName: {}", LOG_PREFIX, userName);
                return null;
            }
        } catch (Exception e) {
            log.error("{}|{}|Failed to extract userId for userName: {}: {}", LOG_PREFIX, ERROR, userName, e.getMessage(), e);
            return null;
        }

    }

    public static String extractUsername(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(input);

        return matcher.matches() ? input.split("@")[0] : input;
    }

    public Map<String, String> createAccessToken(String userId, String tenantId, String productId) throws BaseException {
        log.debug("{}|{}|Creating access token for userId: {}, tenantId: {}, productType: {}", 
            LOG_PREFIX, START, userId, tenantId, productId);
        
        Map<String, Object> claimsMap = new HashMap<>();
        Map<String, String> tokenMap = new HashMap<>();

        try {
            CommonSouthBoundResponse<UserDetailsForToken> infoByUserId = crmUserDetailClient.getUserDetails(userId, productId);
            CommonSouthBoundResponse<UserGroupDto> userGroupResponse = crmUserDetailClient.getUserGroupDetails(userId, productId);

            claimsMap.put(ServiceConstants.PERMISSIONS, infoByUserId.getResponseData().getPermission());
            claimsMap.put(ServiceConstants.EMAIL, infoByUserId.getResponseData().getEmail());
            claimsMap.put(ServiceConstants.U_NAME, infoByUserId.getResponseData().getName());
            claimsMap.put(ServiceConstants.TENANT_ID, tenantId);
            claimsMap.put(ServiceConstants.AUTH_SYSTEM_IDS, getAuthSystemIdList(infoByUserId.getResponseData().getPermission(), infoByUserId.getResponseData().getUserId()));
            claimsMap.put(ServiceConstants.CUSTOM_PROPERTIES, getCustomProperties(userId));
            claimsMap.put(ServiceConstants.ROLES, userGroupResponse.getResponseData().getRoleIds());
            claimsMap.put(ServiceConstants.GROUPS, userGroupResponse.getResponseData().getGroups());
            claimsMap.put(ServiceConstants.USER_ID, userGroupResponse.getResponseData().getUserId());

            jwtService.generateAccessToken(userId, claimsMap, tokenMap);
            log.debug("{}|{}|Access token generated for userId: {}", LOG_PREFIX, SUCCESS, userId);
            return tokenMap;

        } catch (RuntimeException ex) {
            log.error("{}|{}|RuntimeException during access token creation for userId: {}: {}", 
                LOG_PREFIX, ERROR, userId, ex.getMessage(), ex);
            throw new BaseException(
                ex.getMessage(),
                AuthCodeEnum.CREATE_ACCESS_TOKEN_FAILED.description(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                AuthCodeEnum.CREATE_ACCESS_TOKEN_FAILED.code(),
                ex.getStackTrace()
            );
        }
    }

    @Override
    public CommonNorthBoundResponse<AccessTokenResponse> newAccessToken(String requestVerificationToken, HttpServletRequest request, String productType) throws BaseException {
        log.info("{}|{}|Generating new access token for productType: {}", LOG_PREFIX, START, productType);
        try {
            String jwtToken = jwtService.tokenExtractor(request);
            Date expTime = jwtService.extractExpiration(jwtToken);

            if (((expTime.getTime() - new Date().getTime()) / 1000) > refreshTimeRange) {
                log.warn("{}|Not within refresh time range for token", LOG_PREFIX);
                throw new BaseException(
                    AuthCodeEnum.NOT_IN_REFRESH_TIME.description(),
                    AuthCodeEnum.NOT_IN_REFRESH_TIME.description(),
                    HttpStatus.BAD_REQUEST,
                    AuthCodeEnum.NOT_IN_REFRESH_TIME.code(),
                    null
                );
            }

            String userId = jwtService.extractUsername(jwtToken);
            Map<String, String> accessTokenMap = createAccessToken(jwtToken, null, productType);

            String newRequestVerificationToken = UUID.randomUUID().toString();
            cacheRepository.save(prefixAC + userId, newRequestVerificationToken, timeLimitAC);

            log.info("{}|{}|New access token created for userId: {}", LOG_PREFIX, SUCCESS, userId);
            return responseHandler.responseBuilder(
                new AccessTokenResponse(accessTokenMap.get(ServiceConstants.TOKEN), newRequestVerificationToken),
                AuthCodeEnum.LOGIN_SUCCESS.description(),
                AuthCodeEnum.LOGIN_SUCCESS.code()
            );

        } catch (BaseException ex) {
            log.error("{}|{}|BaseException during new access token generation: {}", LOG_PREFIX, ERROR, ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("{}|{}|Unexpected error during new access token generation: {}", LOG_PREFIX, ERROR, ex.getMessage(), ex);
            throw new BaseException(
                ex.getMessage(),
                AuthCodeEnum.CREATE_ACCESS_TOKEN_FAILED.description(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                AuthCodeEnum.CREATE_ACCESS_TOKEN_FAILED.code(),
                ex.getStackTrace()
            );
        }
    }

    @Override
    public CommonNorthBoundResponse<String> userLogOut(HttpServletRequest httpServletRequest) {
        log.info("{}|{}|Processing user logout request", LOG_PREFIX, START);
        try {
            String userName = jwtService.extractUsername(jwtService.tokenExtractor(httpServletRequest));
            log.info("{}|User logout request for userName: {}", LOG_PREFIX, userName);

            if (cacheRepository.existsByUserId(userName)) {
                cacheRepository.delete(userName);
                log.info("{}|User session cleared for userName: {}", LOG_PREFIX, userName);
            }

            return responseHandler.responseBuilder(
                null,
                AuthCodeEnum.AUTH_REQUEST_SUCCESS.code(),
                AuthCodeEnum.AUTH_REQUEST_SUCCESS.description()
            );

        } catch (BaseException ex) {
            log.error("{}|{}|BaseException during logout: {}", LOG_PREFIX, ERROR, ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("{}|{}|Unexpected error during logout: {}", LOG_PREFIX, ERROR, ex.getMessage(), ex);
            throw new BaseException(
                ex.getMessage(),
                AuthCodeEnum.LOGOUT_INTERNAL_SERVER_ERROR.description(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                AuthCodeEnum.LOGOUT_INTERNAL_SERVER_ERROR.code(),
                ex.getStackTrace()
            );
        }
    }

    @Override
    public CommonNorthBoundResponse<AccessTokenResponse> enhanceAccessToken(TokenEnhancementRequest request, String rvToken, HttpServletRequest httpServletRequest, String productType) {
        log.info("{}|{}|Enhancing token for login system: {}", LOG_PREFIX, START, request.getLoginSystemId());
        try {
            String jwtToken = jwtService.tokenExtractor(httpServletRequest);
            Date expTime = jwtService.extractExpiration(jwtToken);

            if (((expTime.getTime() - new Date().getTime()) / 1000) > refreshTimeRange) {
                log.warn("{}|Not within refresh time range for token", LOG_PREFIX);
                throw new BaseException(
                    AuthCodeEnum.NOT_IN_REFRESH_TIME.description(),
                    AuthCodeEnum.NOT_IN_REFRESH_TIME.description(),
                    HttpStatus.BAD_REQUEST,
                    AuthCodeEnum.NOT_IN_REFRESH_TIME.code(),
                    null
                );
            }

            String userId = jwtService.extractUsername(jwtToken);
            Map<String, String> accessTokenMap = createAccessToken(jwtToken, null, productType);

            String newRequestVerificationToken = UUID.randomUUID().toString();
            cacheRepository.save(prefixAC + userId, newRequestVerificationToken, timeLimitAC);

            log.info("{}|{}|New access token created for userId: {}", LOG_PREFIX, SUCCESS, userId);
            return responseHandler.responseBuilder(
                new AccessTokenResponse(accessTokenMap.get(ServiceConstants.TOKEN), newRequestVerificationToken),
                AuthCodeEnum.LOGIN_SUCCESS.description(),
                AuthCodeEnum.LOGIN_SUCCESS.code()
            );

        } catch (BaseException ex) {
            log.error("{}|{}|BaseException during token enhancement: {}", LOG_PREFIX, ERROR, ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("{}|{}|Unexpected error during token enhancement: {}", LOG_PREFIX, ERROR, ex.getMessage(), ex);
            throw new BaseException(
                ex.getMessage(),
                AuthCodeEnum.CREATE_ACCESS_TOKEN_FAILED.description(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                AuthCodeEnum.CREATE_ACCESS_TOKEN_FAILED.code(),
                ex.getStackTrace()
            );
        }
    }

    private List<Integer> getAuthSystemIdList(PermissionDTO permission, Long userId) {
        List<Integer> accessSystemList = crmUserDetailClient.getUserAccessSystem(String.valueOf(userId)).getResponseData();

//        List<Integer> permissionAccessList = permission.getComponents().stream()
//                .map(view -> view.getProductDetail().getId())
//                .distinct()
//                .collect(Collectors.toList());

        return Stream.concat(
                        accessSystemList.stream(),
                        permission.getComponents().stream()
                                .map(view -> view.getProductDetail().getId())
                )
                .distinct()
                .collect(Collectors.toList());
    }

    private List<CustomPropertyDTO> getCustomProperties(String userName) {
        log.debug("{}|{}|Fetching custom properties for userName: {}", LOG_PREFIX, START, userName);
        try {
            List<CustomPropertyDTO> properties = customPropertiesClient.getAllCustomPropertiesByUserId(userName).getResponseData();
            log.debug("{}|{}|Successfully fetched custom properties for userName: {}", LOG_PREFIX, SUCCESS, userName);
            return properties;
        } catch (Exception e) {
            log.error("{}|{}|Failed to fetch custom properties for userName: {}: {}", 
                LOG_PREFIX, ERROR, userName, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private boolean isUserHaveAccessToSystem(String userId, String productId) {
        List<Integer> accessSystemList = crmUserDetailClient.getUserAccessSystem(userId).getResponseData();

        if (accessSystemList != null && !accessSystemList.isEmpty()) {
            int targetProductId = Integer.parseInt(productId);

            for (Integer systemId : accessSystemList) {
                if (systemId == targetProductId) {
                    log.debug("{} | User has access to the system: {}", LOG_PREFIX, productId);
                    return true;
                }
            }
        }

        return false;
    }

}
