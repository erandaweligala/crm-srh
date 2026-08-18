package com.adl.et.telco.crm.securerequesthandler.application.controller.access;

import com.adl.et.telco.crm.securerequesthandler.application.controller.BaseController;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonNorthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.security.AccessTokenResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.security.SamlRequest;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.security.TokenResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.access.JwtService;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.common.MicroServiceURLFetchService;
import com.adl.et.telco.crm.securerequesthandler.external.service.saml.CookieService;
import com.adl.et.telco.crm.securerequesthandler.external.service.saml.SamlResponseService;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.access.UserAuthenticationManageService;
import com.adl.et.telco.crm.securerequesthandler.application.util.constants.Constants;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.AuthCodeEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

/**
 * Controller for handling Azure AD authentication and SAML requests.
 * Provides endpoints for SAML request creation and validation.
 */
@Slf4j
@RestController
@RequestMapping("/srh/auth")
public class ADAuthenticationController extends BaseController {
    private static final String LOG_PREFIX = "SRH|ADAuthenticationController|";

    @Value("${login.success.url}")
    private String adLoginSuccessURL;

    @Value("${login.fail.url}")
    private String adLoginFailUrl;

    private final UserAuthenticationManageService userAuthenticationManageService;
    private final SamlResponseService samlResponseService;
    private final CookieService cookieService;

    public ADAuthenticationController(
            JwtService jwtService,
            MicroServiceURLFetchService microServiceURLFetchService,
            UserAuthenticationManageService userAuthenticationManageService,
            SamlResponseService samlResponseService,
            CookieService cookieService) {

        super(jwtService, microServiceURLFetchService);
        this.userAuthenticationManageService = userAuthenticationManageService;
        this.samlResponseService = samlResponseService;
        this.cookieService = cookieService;
    }

    /**
     * Creates a SAML2 request for Azure AD authentication.
     *
     * @param httpServletRequest The HTTP request
     * @return ResponseEntity containing the SAML request
     * @throws IOException if there's an error creating the request
     */
    @GetMapping("/azure-ad-auth/saml2-request")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<CommonNorthBoundResponse<SamlRequest>> createSamlRequest(
            HttpServletRequest httpServletRequest) throws IOException {
        log.debug("{}createSamlRequest|Start", LOG_PREFIX);
        
        UUID uuid = UUID.randomUUID();
        CommonNorthBoundResponse<SamlRequest> samlRequest = userAuthenticationManageService.createSamlRequest(uuid.toString());
        
        log.info("{}createSamlRequest|End|SAML request created successfully", LOG_PREFIX);
        return setResponseEntity(samlRequest);
    }

    /**
     * Validates the SAML token and redirects with appropriate tokens.
     *
     * @param saml The SAML response
     * @param response The HTTP response
     * @param httpServletRequest The HTTP request
     * @return ResponseEntity with redirect location
     */
    @PostMapping("/azure-ad-auth/saml-res")
    public ResponseEntity<AccessTokenResponse> validateSAMLTokenAndRedirect(
            @RequestParam("SAMLResponse") String saml,
            HttpServletResponse response,
            HttpServletRequest httpServletRequest) {
        log.debug("{}validateSAMLTokenAndRedirect|Start|Validating SAML token", LOG_PREFIX);

        CommonNorthBoundResponse<TokenResponse> validatedSamlResponse = samlResponseService.createTempToken(saml);
        String redirectUrl;

        if (validatedSamlResponse.getCode().equals(AuthCodeEnum.LOGIN_SUCCESS.code())) {
            response.addCookie(cookieService.createCookie(validatedSamlResponse.getData().getRequestVerificationToken()));
            redirectUrl = adLoginSuccessURL + validatedSamlResponse.getData().getTempToken();
            log.info("{}validateSAMLTokenAndRedirect|End|Login successful", LOG_PREFIX);
        } else if (validatedSamlResponse.getCode().equals(AuthCodeEnum.USER_NOT_FOUND.code())) {
            log.error("{}validateSAMLTokenAndRedirect|Error|User not found: {}", LOG_PREFIX, validatedSamlResponse.getDescription());
            redirectUrl = adLoginFailUrl + Constants.USER_NOT_FOUN_PATH;
        } else if (validatedSamlResponse.getCode().equals(AuthCodeEnum.INACTIVE_USER.code())) {
            log.error("{}validateSAMLTokenAndRedirect|Error|Inactive user: {}", LOG_PREFIX, validatedSamlResponse.getDescription());
            redirectUrl = adLoginFailUrl + Constants.INACTIVE_USER_PATH;
        } else {
            log.error("{}validateSAMLTokenAndRedirect|Error|Internal error: {}", LOG_PREFIX, validatedSamlResponse.getDescription());
            redirectUrl = adLoginFailUrl + Constants.INTERNAL_ERROR_PATH;
        }

        return ResponseEntity.status(302).location(URI.create(redirectUrl)).build();
    }
}
