package com.adl.et.telco.crm.securerequesthandler.application.controller.access;

import com.adl.et.telco.crm.securerequesthandler.application.controller.BaseController;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonNorthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.security.AccessTokenResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.security.TokenEnhancementRequest;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.security.UserLoginRequest;
import com.adl.et.telco.crm.securerequesthandler.external.service.saml.CookieService;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.access.UserAuthenticationService;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.access.JwtService;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.common.MicroServiceURLFetchService;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.BaseException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling user authentication operations.
 * Provides endpoints for login, logout, and token management.
 */
@Slf4j
@RestController
@RequestMapping("/srh/auth")
public class UserAuthenticationController extends BaseController {
    private static final String LOG_PREFIX = "SRH|UserAuthenticationController|";

    private final UserAuthenticationService userAuthenticationService;
    private final CookieService cookieService;

    public UserAuthenticationController(JwtService jwtService, MicroServiceURLFetchService microServiceURLFetchService, UserAuthenticationService userAuthenticationService, CookieService cookieService) {
        super(jwtService, microServiceURLFetchService);
        this.userAuthenticationService = userAuthenticationService;
        this.cookieService = cookieService;
    }

    /**
     * Authenticates a user and creates an access token.
     *
     * @param userLoginRequest The login request containing user credentials
     * @param applicationId Optional product type for the login
     * @param response The HTTP response for setting cookies
     * @param rvToken The request verification token from cookie
     * @param httpServletRequest The HTTP request
     * @return ResponseEntity containing the access token response
     * @throws BaseException if authentication fails
     */
    @PostMapping("/user/login")
    public ResponseEntity<CommonNorthBoundResponse<AccessTokenResponse>> userLogin(
            @RequestBody UserLoginRequest userLoginRequest,
            @RequestHeader(value = "X-Application-Id") String applicationId,
            HttpServletResponse response,
            @CookieValue(value = "rv_token", defaultValue = "") String rvToken,
            HttpServletRequest httpServletRequest) throws BaseException {
        
        long startTime = System.currentTimeMillis();
        log.debug("{}userLogin|Start|Code: {}|ApplicationId: {}|RVTokenPresent: {}",
            LOG_PREFIX, userLoginRequest.getCode(), applicationId, !rvToken.isEmpty());
        
        try {
            CommonNorthBoundResponse<AccessTokenResponse> accessToken = 
                userAuthenticationService.login(rvToken, userLoginRequest, applicationId);
            
            response.addCookie(cookieService.createCookie(accessToken.getData().getRequestVerificationToken()));
            log.info("{}userLogin|End|Success|Code: {}|Duration: {} ms", 
                LOG_PREFIX, userLoginRequest.getCode(), 
                (System.currentTimeMillis() - startTime));
            
            return setResponseEntity(accessToken);
        } catch (Exception e) {
            log.error("{}userLogin|Error|Code: {}|Error: {}", 
                LOG_PREFIX, userLoginRequest.getCode(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Logs out the current user and invalidates their session.
     *
     * @param httpServletRequest The HTTP request
     * @return ResponseEntity containing the logout response
     * @throws BaseException if logout fails
     */
    @DeleteMapping("/user/logout")
    public ResponseEntity<CommonNorthBoundResponse<String>> userLogOut(
            HttpServletRequest httpServletRequest) throws BaseException {
        
        long startTime = System.currentTimeMillis();
        log.debug("{}userLogOut|Start", LOG_PREFIX);
        
        try {
            CommonNorthBoundResponse<String> response = 
                userAuthenticationService.userLogOut(httpServletRequest);
            
            log.info("{}userLogOut|End|Success|Duration: {} ms", 
                LOG_PREFIX, (System.currentTimeMillis() - startTime));
            
            return setResponseEntity(response);
        } catch (Exception e) {
            log.error("{}userLogOut|Error|Error: {}", LOG_PREFIX, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Generates a new access token using the existing token.
     *
     * @param httpServletRequest The HTTP request
     * @param response The HTTP response for setting cookies
     * @param productType Optional product type
     * @param rvToken The request verification token from cookie
     * @return ResponseEntity containing the new access token
     * @throws BaseException if token generation fails
     */
    @GetMapping("/user/new-access-token")
    public ResponseEntity<CommonNorthBoundResponse<AccessTokenResponse>> getNewAccessToken(
            HttpServletRequest httpServletRequest,
            HttpServletResponse response,
            @RequestParam(value = "productType", required = false) String productType,
            @CookieValue(value = "rv_token", defaultValue = "") String rvToken) throws BaseException {
        
        long startTime = System.currentTimeMillis();
        log.debug("{}getNewAccessToken|Start|ProductType: {}|RVTokenPresent: {}", 
            LOG_PREFIX, productType, !rvToken.isEmpty());
        
        try {
            CommonNorthBoundResponse<AccessTokenResponse> accessToken = 
                userAuthenticationService.newAccessToken(rvToken, httpServletRequest, productType);
            
            response.addCookie(cookieService.createCookie(accessToken.getData().getRequestVerificationToken()));
            log.info("{}getNewAccessToken|End|Success|Duration: {} ms", 
                LOG_PREFIX, (System.currentTimeMillis() - startTime));
            
            return setResponseEntity(accessToken);
        } catch (Exception e) {
            log.error("{}getNewAccessToken|Error|Error: {}", LOG_PREFIX, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Enhances an existing access token with additional claims.
     *
     * @param httpServletRequest The HTTP request
     * @param request The token enhancement request
     * @param response The HTTP response for setting cookies
     * @param rvToken The request verification token from cookie
     * @param productType Optional product type
     * @return ResponseEntity containing the enhanced access token
     */
    @PostMapping("/user/enhance/token")
    public ResponseEntity<CommonNorthBoundResponse<AccessTokenResponse>> getEnhanceAccessToken(
            HttpServletRequest httpServletRequest,
            @RequestBody TokenEnhancementRequest request,
            HttpServletResponse response,
            @CookieValue(value = "rv_token", defaultValue = "") String rvToken,
            @RequestParam(value = "productType", required = false) String productType) {
        
        long startTime = System.currentTimeMillis();
        log.debug("{}getEnhanceAccessToken|Start|ProductType: {}|RVTokenPresent: {}", 
            LOG_PREFIX, productType, !rvToken.isEmpty());
        
        try {
            CommonNorthBoundResponse<AccessTokenResponse> accessToken = 
                userAuthenticationService.enhanceAccessToken(request, rvToken, httpServletRequest, productType);
            
            response.addCookie(cookieService.createCookie(accessToken.getData().getRequestVerificationToken()));
            log.info("{}getEnhanceAccessToken|End|Success|Duration: {} ms", 
                LOG_PREFIX, (System.currentTimeMillis() - startTime));
            
            return setResponseEntity(accessToken);
        } catch (Exception e) {
            log.error("{}getEnhanceAccessToken|Error|Error: {}", LOG_PREFIX, e.getMessage(), e);
            throw e;
        }
    }
}
