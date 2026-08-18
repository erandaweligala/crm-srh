/*
 * Copyrights 2020 Axiata Digital Labs Pvt Ltd.
 * All Rights Reserved.
 * <p>
 * These material are unpublished, proprietary, confidential source
 * code of Axiata Digital Labs Pvt Ltd (ADL) and constitute a TRADE
 * SECRET of ADL.
 * <p>
 * ADL retains all title to and intellectual property rights in these
 * materials.
 */

package com.adl.et.telco.crm.securerequesthandler.external.service.saml;

import com.adl.et.telco.crm.securerequesthandler.application.client.accessclient.CrmUserDetailClient;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.authentication.UserBasicInfo;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonNorthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonSouthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.security.SamlUserData;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.security.TokenResponse;
import com.adl.et.telco.crm.securerequesthandler.external.repository.cache.AuthCacheRepository;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.access.JwtService;
import com.adl.et.telco.crm.securerequesthandler.application.util.ResponseHandler;
import com.adl.et.telco.crm.securerequesthandler.application.util.constants.ServiceConstants;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.BaseException;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.AuthCodeEnum;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.ResponseCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

@Service
@Slf4j
public class SamlResponseService {
    private static final String LOG_PREFIX = "SRH|SamlResponseService|";
    private static final String START = "START";
    private static final String END = "END";
    private static final String ERROR = "ERROR";
    private static final String SUCCESS = "SUCCESS";

    private final ResponseHandler handler;
    private final String publicKeyCertificatePath;
    private final Long tempTokenExpTime;
    private final String tempTokenPrefix;
    private final CommonSecurityService commonSecurityService;
    private final CrmUserDetailClient userDetailClient;
    private final AuthCacheRepository cacheRepository;
    private final JwtService jwtService;
    private final ResourceLoader resourceLoader;

    public SamlResponseService(
            ResponseHandler handler,
            @Value("${saml.public-key-certificate.path}") String publicKeyCertificatePath,
            @Value("${temp.token.ttl.sec}") Long tempTokenExpTime,
            @Value("${prefix.temp}") String tempTokenPrefix,
            CommonSecurityService commonSecurityService,
            CrmUserDetailClient userDetailClient,
            AuthCacheRepository cacheRepository,
            JwtService jwtService,
            ResourceLoader resourceLoader) throws InitializationException {
        this.handler = handler;
        this.publicKeyCertificatePath = publicKeyCertificatePath;
        this.tempTokenExpTime = tempTokenExpTime;
        this.tempTokenPrefix = tempTokenPrefix;
        this.commonSecurityService = commonSecurityService;
        this.userDetailClient = userDetailClient;
        this.cacheRepository = cacheRepository;
        this.jwtService = jwtService;
        this.resourceLoader = resourceLoader;
        InitializationService.initialize();
    }

    public CommonNorthBoundResponse<TokenResponse> createTempToken(String saml) {
        log.debug("{}|{}|Creating temp token from SAML response", LOG_PREFIX, START);
        try {
            Document samlDocument = convertStringToXMLDocument(saml);
            if (samlDocument != null) {
                Element samlDocumentElement = samlDocument.getDocumentElement();
                log.debug("{}|SAML Element retrieved successfully", LOG_PREFIX);
                XMLObject samlXMLObject = unmarshallSamlDocElement(samlDocumentElement);
                log.debug("{}|SAML Element unmarshalled successfully", LOG_PREFIX);
                return getTempTokenFromSamlXmlObject(samlXMLObject);
            } else {
                log.error("{}|{}|Invalid SAML document", LOG_PREFIX, ERROR);
                return accessFailureHandler(AuthCodeEnum.INVALID_SAML_DOCUMENT);
            }
        } catch (RuntimeException | ParserConfigurationException ex) {
            log.error("{}|{}|Error processing SAML response: {}", LOG_PREFIX, ERROR, ex.getMessage(), ex);
            return accessFailureHandler(AuthCodeEnum.EXCEPTION_SERVICE_LAYER);
        }
    }

    private Document convertStringToXMLDocument(String xmlString) throws ParserConfigurationException {
        log.debug("{}|{}|Converting string to XML document", LOG_PREFIX, START);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        
        // Disable external entity access
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        
        // Enable namespace awareness
        factory.setNamespaceAware(true);
        
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            byte[] base64DecodedResponse = Base64.decodeBase64(xmlString);
            Document doc = builder.parse(new InputSource(new StringReader(new String(base64DecodedResponse, StandardCharsets.UTF_8))));
            log.debug("{}|{}|XML document created successfully", LOG_PREFIX, END);
            return doc;
        } catch (Exception e) {
            log.error("{}|{}|Failed to convert SAML to XML: {}", LOG_PREFIX, ERROR, e.getMessage(), e);
            return null;
        }
    }

    private CommonNorthBoundResponse<TokenResponse> getTempTokenFromSamlXmlObject(XMLObject samlXMLObject) {
        log.debug("{}|{}|Getting temp token from SAML XML object", LOG_PREFIX, START);
        
        if (samlXMLObject == null) {
            log.error("{}|{}|Invalid SAML object", LOG_PREFIX, ERROR);
            return accessFailureHandler(AuthCodeEnum.INVALID_SAML_OBJECT);
        }

        try {
            Response samlResponse = (Response) samlXMLObject;
            SamlUserData samlUserData = getSamlUserData(samlResponse);
            boolean isValidSaml = validateSaml(samlResponse);

            CommonSouthBoundResponse<UserBasicInfo> userBasicInfo = userDetailClient.getBasicUserDetails(samlUserData.getEmail());
            boolean isEligible = false;

            if (userBasicInfo.getResponseData() != null) {
                isEligible = commonSecurityService.isEligible(userBasicInfo);
            }

            if (isValidSaml && userBasicInfo.getResponseData() != null && isEligible) {
                log.debug("{}|{}|Successfully validated SAML and user eligibility", LOG_PREFIX, SUCCESS);
                return handler.responseBuilder(
                    createTempTokenFromBasicUserInfo(userBasicInfo.getResponseData()),
                    AuthCodeEnum.LOGIN_SUCCESS.description(),
                    AuthCodeEnum.LOGIN_SUCCESS.code()
                );
            } else if (userBasicInfo.getResponseData() == null) {
                log.error("{}|{}|User not found", LOG_PREFIX, ERROR);
                return accessFailureHandler(AuthCodeEnum.USER_NOT_FOUND);
            } else if (!isEligible) {
                log.error("{}|{}|User is not eligible", LOG_PREFIX, ERROR);
                return accessFailureHandler(AuthCodeEnum.INACTIVE_USER);
            } else {
                log.error("{}|{}|Invalid SAML response", LOG_PREFIX, ERROR);
                return accessFailureHandler(AuthCodeEnum.INVALID_SAML_RESPONSE);
            }
        } catch (Exception e) {
            log.error("{}|{}|Error processing SAML XML object: {}", LOG_PREFIX, ERROR, e.getMessage(), e);
            return accessFailureHandler(AuthCodeEnum.EXCEPTION_SERVICE_LAYER);
        }
    }

    private SamlUserData getSamlUserData(Response samlResponse) throws BaseException {
        log.debug("{}|{}|Extracting SAML user data", LOG_PREFIX, START);
        try {
            SamlUserData samlUserData = new SamlUserData();
            Assertion assertion = samlResponse.getAssertions().get(0);
            samlUserData.setEmail(assertion.getSubject().getNameID().getValue());
            samlUserData.setRequestId(assertion.getSubject().getSubjectConfirmations().get(0)
                    .getSubjectConfirmationData().getInResponseTo());
            log.debug("{}|{}|SAML user data extracted successfully", LOG_PREFIX, END);
            return samlUserData;
        } catch (Exception e) {
            log.error("{}|{}|Failed to extract SAML user data: {}", LOG_PREFIX, ERROR, e.getMessage(), e);
            throw new BaseException(e.getMessage(), ResponseCodeEnum.EXCEPTION_SERVICE_LAYER.description(),
                    HttpStatus.INTERNAL_SERVER_ERROR, ResponseCodeEnum.EXCEPTION_SERVICE_LAYER.code(), e.getStackTrace());
        }
    }

    private boolean validateSaml(Response samlResponse) throws BaseException {
        log.debug("{}|{}|Validating SAML response", LOG_PREFIX, START);
        boolean isValidSaml = false;
        
        try {
            Assertion assertion = samlResponse.getAssertions().get(0);
            Signature samlSignature = assertion.getSignature();
            Resource resource = resourceLoader.getResource("classpath:" + publicKeyCertificatePath);

            if (!resource.exists()) {
                log.error("{}|{}|Public key certificate not found", LOG_PREFIX, ERROR);
                return false;
            }

            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            try (InputStream fileStream = resource.getInputStream()) {
                X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(fileStream);
                BasicX509Credential publicCredential = new BasicX509Credential(certificate);

                X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(certificate.getPublicKey().getEncoded());
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PublicKey key = keyFactory.generatePublic(publicKeySpec);

                if (key != null) {
                    publicCredential.setEntityCertificate(certificate);
                    SignatureValidator.validate(samlSignature, publicCredential);
                    isValidSaml = true;
                    log.debug("{}|{}|SAML validation successful", LOG_PREFIX, SUCCESS);
                }
            }
        } catch (IOException | CertificateException | NoSuchAlgorithmException | InvalidKeySpecException | SignatureException e) {
            log.error("{}|{}|SAML validation failed: {}", LOG_PREFIX, ERROR, e.getMessage(), e);
            throw new BaseException(e.getMessage(), ResponseCodeEnum.EXCEPTION_SERVICE_LAYER.description(),
                    HttpStatus.INTERNAL_SERVER_ERROR, ResponseCodeEnum.EXCEPTION_SERVICE_LAYER.code(), e.getStackTrace());
        }

        return isValidSaml;
    }

    private TokenResponse createTempTokenFromBasicUserInfo(UserBasicInfo basicInfo) throws BaseException {
        log.debug("{}|{}|Creating temp token from basic user info", LOG_PREFIX, START);
        String requestVerificationToken = generateRVT();
        Map<String, String> tempTokenMap = createTempToken(basicInfo);
        
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setTempToken(tempTokenMap.get(ServiceConstants.TOKEN));
        tokenResponse.setRequestVerificationToken(requestVerificationToken);

        cacheRepository.save(tempTokenPrefix + basicInfo.getUserId(), requestVerificationToken, tempTokenExpTime);
        log.debug("{}|{}|Temp token created successfully", LOG_PREFIX, END);
        return tokenResponse;
    }

    private String generateRVT() {
        log.debug("{}|{}|Generating request verification token", LOG_PREFIX, START);
        String rvt = UUID.randomUUID().toString();
        log.debug("{}|{}|Request verification token generated", LOG_PREFIX, END);
        return rvt;
    }

    private XMLObject unmarshallSamlDocElement(Element samlDocumentElement) throws BaseException {
        log.debug("{}|{}|Unmarshalling SAML document element", LOG_PREFIX, START);
        try {
            Unmarshaller unmarshaller = org.opensaml.core.xml.util.XMLObjectSupport.getUnmarshaller(samlDocumentElement);
            XMLObject unmarshall = unmarshaller.unmarshall(samlDocumentElement);
            log.debug("{}|{}|SAML document element unmarshalled successfully", LOG_PREFIX, END);
            return unmarshall;
        } catch (Exception ex) {
            log.error("{}|{}|Failed to unmarshal SAML document: {}", LOG_PREFIX, ERROR, ex.getMessage(), ex);
            throw new BaseException(ex.getMessage(), ResponseCodeEnum.EXCEPTION_SERVICE_LAYER.description(),
                    HttpStatus.INTERNAL_SERVER_ERROR, ResponseCodeEnum.EXCEPTION_SERVICE_LAYER.code(), ex.getStackTrace());
        }
    }

    private Map<String, String> createTempToken(UserBasicInfo userBasicInfo) throws BaseException {
        log.debug("{}|{}|Creating temp token", LOG_PREFIX, START);
        try {
            Map<String, String> tempTokenMap = new HashMap<>();
            Map<String, Object> claimsMap = new HashMap<>();
            claimsMap.put(ServiceConstants.EMAIL, userBasicInfo.getEmail());
            jwtService.generateAccessToken(userBasicInfo.getUserId(), claimsMap, tempTokenMap);
            log.debug("{}|{}|Temp token created successfully", LOG_PREFIX, END);
            return tempTokenMap;
        } catch (Exception ex) {
            log.error("{}|{}|Failed to create temp token: {}", LOG_PREFIX, ERROR, ex.getMessage(), ex);
            throw new BaseException(ex.getMessage(), ResponseCodeEnum.EXCEPTION_SERVICE_LAYER.description(),
                    HttpStatus.INTERNAL_SERVER_ERROR, ResponseCodeEnum.EXCEPTION_SERVICE_LAYER.code(), ex.getStackTrace());
        }
    }

    private CommonNorthBoundResponse<TokenResponse> accessFailureHandler(AuthCodeEnum respCodeEnum) {
        log.debug("{}|{}|Handling access failure with code: {}", LOG_PREFIX, START, respCodeEnum.code());
        return handler.northBoundRespHandler(respCodeEnum.code(), respCodeEnum.description());
    }
}



