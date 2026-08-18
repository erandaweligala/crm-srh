package com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.access;

import com.adl.et.telco.crm.securerequesthandler.application.util.exception.BaseException;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.AuthCodeEnum;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.ResponseCodeEnum;
import com.adl.et.telco.crm.securerequesthandler.external.repository.cache.AuthCacheRepository;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {
    private static final String LOG_PREFIX = "SRH|JwtService|";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer";
    private static final String JWT = "JWT";
    private static final String PERMISSIONS = "permissions";
    private static final String COMPONENTS = "components";
    private static final String ACTIDS = "actions";
    private static final String T_TYPE = "tType";
    private static final String ACCESS = "access";
    private static final String IDLE_TIME_RANGE = "idleTimeRange";
    private static final String REFRESH_TIME_RANGE = "refreshTimeRange";
    private static final String TOKEN = "token";
    private static final String SUB = "sub";
    private static final String OTP = "otp";

    private final String secretKey;
    private final Long accessTokenExpireTime;
    private final long idleTimeRange;
    private final long refreshTimeRange;
    private final String tempTokenPrefix;
    private final String prefixAC;
    private final AuthCacheRepository cacheRepository;

    public JwtService(@Value("${assisted-channel.secret-key}") String secretKey,
                     @Value("${access.token-expire-time-millsec}") Long accessTokenExpireTime,
                     @Value("${jwt.idle.time.range.sec}") long idleTimeRange,
                     @Value("${jwt.refresh.time.range.sec}") long refreshTimeRange,
                     @Value("${prefix.temp}") String tempTokenPrefix,
                     @Value("${prefix.ac}") String prefixAC,
                     AuthCacheRepository cacheRepository) {
        this.secretKey = secretKey;
        this.accessTokenExpireTime = accessTokenExpireTime;
        this.idleTimeRange = idleTimeRange;
        this.refreshTimeRange = refreshTimeRange;
        this.tempTokenPrefix = tempTokenPrefix;
        this.prefixAC = prefixAC;
        this.cacheRepository = cacheRepository;
    }

    public String extractUsername(String token) {
        log.debug("{}extractUsername|START|Extracting username from token", LOG_PREFIX);
        String username = extractClaim(token, Claims::getSubject);
        log.debug("{}extractUsername|END|Username extracted: {}", LOG_PREFIX, username);
        return username;
    }

    public String extractTenantId(String token) {
        log.debug("{}extractTenantId|START|Extracting tenant ID from token", LOG_PREFIX);
        String tenantId = extractClaimWithType(token, "tenantId", String.class);
        log.debug("{}extractTenantId|END|Tenant ID extracted: {}", LOG_PREFIX, tenantId);
        return tenantId;
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <Y> Y extractClaimWithType(String token, String claimName, Class<Y> returnClass) {
        return extractAllClaims(token).get(claimName, returnClass);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secretKey.getBytes()).parseClaimsJws(token).getBody();
    }

    public String tokenExtractor(HttpServletRequest httpServletRequest) throws BaseException {
        log.debug("{}tokenExtractor|START|Extracting token from request", LOG_PREFIX);
        try {
            String jwtToken = "";
            final String authorizationHeader = httpServletRequest.getHeader(AUTHORIZATION);
            if (authorizationHeader != null && authorizationHeader.startsWith(BEARER)) {
                jwtToken = authorizationHeader.substring(7);
            }
            log.debug("{}tokenExtractor|END|Token extracted: {}", LOG_PREFIX, jwtToken.isEmpty() ? "empty" : "present");
            return jwtToken;
        } catch (Exception ex) {
            log.error("{}tokenExtractor|ERROR|Error extracting token: {}", LOG_PREFIX, ex.getMessage());
            throw new BaseException(ex.getMessage(), ResponseCodeEnum.EXCEPTION_SERVICE_LAYER.description(), 
                HttpStatus.INTERNAL_SERVER_ERROR, ResponseCodeEnum.EXCEPTION_SERVICE_LAYER.code(), ex.getStackTrace());
        }
    }

    public List<GrantedAuthority> extractAuthorities(String token) throws BaseException {
        log.debug("{}extractAuthorities|START|Extracting authorities from token", LOG_PREFIX);
        try {
            List<GrantedAuthority> authorities = new ArrayList<>();
            String encodedPayload = token.split("\\.")[1];
            String claimsString = new String(Base64.getDecoder().decode(encodedPayload), StandardCharsets.UTF_8);

            JSONObject claimsJsonObject = new JSONObject(claimsString);
            JSONObject tempJsonObj;
            JSONArray tempJsonArr;
            String authority;

            if (claimsJsonObject.has(PERMISSIONS)) {
                JSONObject permissions = ((JSONObject) claimsJsonObject.get(PERMISSIONS));
                JSONArray views = (JSONArray) (permissions.get(COMPONENTS));
                
                for (int i = 0; i < views.length(); i++) {
                    tempJsonObj = (JSONObject) views.get(i);
                    tempJsonArr = (JSONArray) tempJsonObj.get(ACTIDS);
                    for (int k = 0; k < tempJsonArr.length(); k++) {
                        authority = String.valueOf(tempJsonArr.getInt(k));
                        authorities.add(new SimpleGrantedAuthority(authority));
                    }
                }
            }
            log.debug("{}extractAuthorities|END|Extracted {} authorities", LOG_PREFIX, authorities.size());
            return authorities;
        } catch (Exception ex) {
            log.error("{}extractAuthorities|ERROR|Error extracting authorities: {}", LOG_PREFIX, ex.getMessage());
            throw new BaseException(ex.getMessage(), ResponseCodeEnum.EXCEPTION_SERVICE_LAYER.description(), 
                HttpStatus.INTERNAL_SERVER_ERROR, ResponseCodeEnum.EXCEPTION_SERVICE_LAYER.code(), ex.getStackTrace());
        }
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public void generateAccessToken(String userId, Map<String, Object> claims, Map<String, String> tokenMap) throws BaseException {
        log.debug("{}generateAccessToken|START|Generating access token for user: {}", LOG_PREFIX, userId);
        try {
            claims.put(T_TYPE, ACCESS);
            claims.put(IDLE_TIME_RANGE, idleTimeRange);
            claims.put(REFRESH_TIME_RANGE, refreshTimeRange);
            tokenMap.put(TOKEN, createToken(userId, claims, accessTokenExpireTime));
            log.debug("{}generateAccessToken|END|Access token generated successfully", LOG_PREFIX);
        } catch (RuntimeException ex) {
            log.error("{}generateAccessToken|ERROR|Error generating access token: {}", LOG_PREFIX, ex.getMessage());
            throw new BaseException(ex.getMessage(), ResponseCodeEnum.EXCEPTION_SERVICE_LAYER.description(), 
                HttpStatus.INTERNAL_SERVER_ERROR, ResponseCodeEnum.EXCEPTION_SERVICE_LAYER.code(), ex.getStackTrace());
        }
    }

    private String createToken(String subject, Map<String, Object> claims, Long expireTime) throws BaseException {
        try {
            Header header = Jwts.header();
            header.setType(JWT);
            return Jwts.builder()
                    .setHeader((Map<String, Object>) header)
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                    .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
                    .compact();
        } catch (Exception ex) {
            log.error("{}createToken|ERROR|Error creating token: {}", LOG_PREFIX, ex.getMessage());
            throw new BaseException(ex.getMessage(), ResponseCodeEnum.EXCEPTION_SERVICE_LAYER.description(), 
                HttpStatus.INTERNAL_SERVER_ERROR, ResponseCodeEnum.EXCEPTION_SERVICE_LAYER.code(), ex.getStackTrace());
        }
    }

    public boolean isValidToken(String token) throws BaseException {
        log.debug("{}isValidToken|START|Validating token", LOG_PREFIX);
        try {
            boolean isValid = false;
            String userId = extractUsername(token);
            String tokenType = extractClaimWithType(token, T_TYPE, String.class);

            if (userId != null && tokenType != null && !isTokenExpired(token)) {
                isValid = tokenType.equals(ACCESS) && cacheRepository.existsByUserId(userId);
            }
            if (isValid && !tokenType.equals(OTP)) {
                cacheRepository.updateExpiryTime(prefixAC + userId, idleTimeRange);
                log.debug("{}isValidToken|INFO|Expiry time updated for cached record with key: {}", LOG_PREFIX, prefixAC + userId);
            }
            log.debug("{}isValidToken|END|Token validation result: {}", LOG_PREFIX, isValid);
            return isValid;

        } catch (ExpiredJwtException ex) {
            log.error("{}isValidToken|ERROR|Token expired: {}", LOG_PREFIX, ex.getMessage());
            throw new BaseException(ex.getMessage(), ResponseCodeEnum.TOKEN_EXPIRED.description(), 
                HttpStatus.FORBIDDEN, ResponseCodeEnum.TOKEN_EXPIRED.code(), ex.getStackTrace());
        } catch (Exception ex) {
            log.error("{}isValidToken|ERROR|Error validating token: {}", LOG_PREFIX, ex.getMessage());
            throw new BaseException(ex.getMessage(), ResponseCodeEnum.EXCEPTION_SERVICE_LAYER.description(), 
                HttpStatus.INTERNAL_SERVER_ERROR, ResponseCodeEnum.EXCEPTION_SERVICE_LAYER.code(), ex.getStackTrace());
        }
    }

    public boolean isValidTempToken(String token) throws BaseException {
        log.debug("{}isValidTempToken|START|Validating temp token", LOG_PREFIX);
        try {
            boolean isValid = false;
            String userId = extractUsername(token);
            String tokenType = extractClaimWithType(token, T_TYPE, String.class);

            if (userId != null && tokenType != null && !isTokenExpired(token)) {
                String receivedUserId = extractClaimWithType(token, SUB, String.class);
                isValid = tokenType.equals(ACCESS) && cacheRepository.existsByKey(tempTokenPrefix + receivedUserId);
            }
            log.debug("{}isValidTempToken|END|Temp token validation result: {}", LOG_PREFIX, isValid);
            return isValid;
        } catch (ExpiredJwtException ex) {
            log.error("{}isValidTempToken|ERROR|Temp token expired: {}", LOG_PREFIX, ex.getMessage());
            throw new BaseException(ex.getMessage(), ResponseCodeEnum.TOKEN_EXPIRED.description(), 
                HttpStatus.FORBIDDEN, ResponseCodeEnum.TOKEN_EXPIRED.code(), ex.getStackTrace());
        } catch (Exception ex) {
            log.error("{}isValidTempToken|ERROR|Error validating temp token: {}", LOG_PREFIX, ex.getMessage());
            throw new BaseException(ex.getMessage(), ResponseCodeEnum.EXCEPTION_SERVICE_LAYER.description(), 
                HttpStatus.INTERNAL_SERVER_ERROR, ResponseCodeEnum.EXCEPTION_SERVICE_LAYER.code(), ex.getStackTrace());
        }
    }

    public boolean isValidRequestVerificationToken(String tempToken, String requestVerificationToken) throws BaseException {
        log.debug("{}isValidRequestVerificationToken|START|Validating request verification token", LOG_PREFIX);
        try {
            boolean isValid;
            String existingRVToken = null;

            String userId = extractClaimWithType(tempToken, SUB, String.class);

            if (cacheRepository.existsByKey(tempTokenPrefix + userId)) {
                existingRVToken = cacheRepository.findByKey(tempTokenPrefix + userId);
            }
            if (existingRVToken == null) {
                log.error("{}isValidRequestVerificationToken|ERROR|Invalid RV token", LOG_PREFIX);
                throw new BaseException(AuthCodeEnum.INVALID_RV_TOKEN.description(), 
                    AuthCodeEnum.INVALID_RV_TOKEN.description(), HttpStatus.UNAUTHORIZED, 
                    AuthCodeEnum.INVALID_RV_TOKEN.code(), null);
            }
            isValid = (requestVerificationToken != null) && requestVerificationToken.equals(existingRVToken);
            log.debug("{}isValidRequestVerificationToken|END|Request verification token validation result: {}", LOG_PREFIX, isValid);
            return isValid;

        } catch (RuntimeException ex) {
            log.error("{}isValidRequestVerificationToken|ERROR|Error validating request verification token: {}", LOG_PREFIX, ex.getMessage());
            throw new BaseException(ex.getMessage(), ResponseCodeEnum.EXCEPTION_SERVICE_LAYER.description(), 
                HttpStatus.INTERNAL_SERVER_ERROR, ResponseCodeEnum.EXCEPTION_SERVICE_LAYER.code(), ex.getStackTrace());
        }
    }

    public boolean isValidRequestVerificationTokenUsingAccessToken(String accessToken, String requestVerificationToken) throws BaseException {
        log.debug("{}isValidRequestVerificationTokenUsingAccessToken|START|Validating request verification token using access token", LOG_PREFIX);
        try {
            boolean isValid;
            String existingRVToken = null;
            String userId = extractUsername(accessToken);

            if (accessToken != null && cacheRepository.existsByKey(prefixAC + userId)) {
                existingRVToken = cacheRepository.findByKey(prefixAC + userId);
            }
            if (existingRVToken == null) {
                log.error("{}isValidRequestVerificationTokenUsingAccessToken|ERROR|Invalid RV token", LOG_PREFIX);
                throw new BaseException(AuthCodeEnum.INVALID_RV_TOKEN.description(), 
                    AuthCodeEnum.INVALID_RV_TOKEN.description(), HttpStatus.UNAUTHORIZED, 
                    AuthCodeEnum.INVALID_RV_TOKEN.code(), null);
            }
            isValid = (requestVerificationToken != null) && requestVerificationToken.equals(existingRVToken);

            if (isValid) {
                cacheRepository.updateExpiryTime(prefixAC + userId, idleTimeRange);
                log.debug("{}isValidRequestVerificationTokenUsingAccessToken|INFO|Expiry time updated for cached record with key: {}", LOG_PREFIX, prefixAC + userId);
            }
            log.debug("{}isValidRequestVerificationTokenUsingAccessToken|END|Request verification token validation result: {}", LOG_PREFIX, isValid);
            return isValid;

        } catch (BaseException e) {
            log.error("{}isValidRequestVerificationTokenUsingAccessToken|ERROR|Base exception: {}", LOG_PREFIX, e.getMessage());
            throw new BaseException(e.getMessage(), e.getReason(), e.getHttpStatus(), e.getResultCode(), null);
        } catch (Exception ex) {
            log.error("{}isValidRequestVerificationTokenUsingAccessToken|ERROR|Error validating request verification token: {}", LOG_PREFIX, ex.getMessage());
            throw new BaseException(ex.getMessage(), ResponseCodeEnum.EXCEPTION_SERVICE_LAYER.description(), 
                HttpStatus.INTERNAL_SERVER_ERROR, ResponseCodeEnum.EXCEPTION_SERVICE_LAYER.code(), ex.getStackTrace());
        }
    }
}



