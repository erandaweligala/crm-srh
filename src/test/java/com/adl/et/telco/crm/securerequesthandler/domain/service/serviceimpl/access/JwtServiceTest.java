package com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.access;

import com.adl.et.telco.crm.securerequesthandler.application.util.exception.BaseException;
import com.adl.et.telco.crm.securerequesthandler.external.repository.cache.AuthCacheRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private AuthCacheRepository cacheRepository;

    @Mock
    private HttpServletRequest httpServletRequest;

    private JwtService jwtService;

    private final String secretKey = "testSecretKeyForTheUnitTests1234@#$TEST";
    private final Long accessTokenExpireTime = 3600000L;
    private final long idleTimeRange = 300L;
    private final long refreshTimeRange = 600L;
    private final String tempTokenPrefix = "temp_";
    private final String prefixAC = "ac_";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                secretKey,
                accessTokenExpireTime,
                idleTimeRange,
                refreshTimeRange,
                tempTokenPrefix,
                prefixAC,
                cacheRepository
        );
    }

    @Test
    void testExtractUsername() {
        String token = createTestToken("testUser", new HashMap<>());
        String username = jwtService.extractUsername(token);
        assertEquals("testUser", username);
    }

    @Test
    void testExtractTenantId() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tenantId", "testTenant");
        String token = createTestToken("testUser", claims);
        String tenantId = jwtService.extractTenantId(token);
        assertEquals("testTenant", tenantId);
    }

    @Test
    void testExtractExpiration() {
        String token = createTestToken("testUser", new HashMap<>());
        Date expiration = jwtService.extractExpiration(token);
        assertNotNull(expiration);
    }

    @Test
    void testTokenExtractor() throws BaseException {
        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer testToken");
        String token = jwtService.tokenExtractor(httpServletRequest);
        assertEquals("testToken", token);
    }

    @Test
    void testTokenExtractor_EmptyHeader() throws BaseException {
        when(httpServletRequest.getHeader("Authorization")).thenReturn(null);
        String token = jwtService.tokenExtractor(httpServletRequest);
        assertEquals("", token);
    }

    @Test
    void testTokenExtractor_Exception() {
        when(httpServletRequest.getHeader("Authorization")).thenThrow(new RuntimeException("Test Exception"));
        assertThrows(BaseException.class, () -> jwtService.tokenExtractor(httpServletRequest));
    }

    @Test
    void testExtractAuthorities() throws Exception {
        Map<String, Object> claims = new HashMap<>();
        Map<String, Object> permissions = new HashMap<>();
        List<Map<String, Object>> components = new ArrayList<>();
        Map<String, Object> component = new HashMap<>();
        List<Integer> actions = new ArrayList<>();

        actions.add(1);
        actions.add(2);
        component.put("actions", actions);
        components.add(component);
        permissions.put("components", components);
        claims.put("permissions", permissions);

        String token = createTestToken("testUser", claims);
        List<GrantedAuthority> authorities = jwtService.extractAuthorities(token);
        assertEquals(2, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("1")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("2")));
    }

    @Test
    void testExtractAuthorities_Exception() {
        assertThrows(BaseException.class, () -> jwtService.extractAuthorities("invalid.token"));
    }

    @Test
    void testIsTokenExpired() {
        Map<String, Object> claims = new HashMap<>();
        String token = createTestToken("testUser", claims);
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void testGenerateAccessToken() throws BaseException {
        Map<String, Object> claims = new HashMap<>();
        Map<String, String> tokenMap = new HashMap<>();
        jwtService.generateAccessToken("testUser", claims, tokenMap);
        assertNotNull(tokenMap.get("token"));
    }

    @Test
    void testGenerateAccessToken_Exception() {
        Map<String, Object> claims = new HashMap<>();
        Map<String, String> tokenMap = new HashMap<>();
        claims.put("invalid", new Object()); // This will cause exception during token creation
        assertThrows(BaseException.class, () -> jwtService.generateAccessToken("testUser", claims, tokenMap));
    }

    @Test
    void testIsValidToken_Valid() throws BaseException {
        String token = createTestToken("testUser", Map.of("tType", "access"));
        when(cacheRepository.existsByUserId("testUser")).thenReturn(true);
        assertTrue(jwtService.isValidToken(token));
    }

    @Test
    void testIsValidToken_Expired() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30";
        assertThrows(BaseException.class, () -> jwtService.isValidToken(token));
    }

    @Test
    void testIsValidTempToken_Valid() throws BaseException {
        String token = createTestToken("testUser", Map.of("tType", "access", "sub", "testUser"));
        when(cacheRepository.existsByKey("temp_testUser")).thenReturn(true);
        assertTrue(jwtService.isValidTempToken(token));
    }

    @Test
    void testIsValidTempToken_Expired() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30";
        assertThrows(BaseException.class, () -> jwtService.isValidTempToken(token));
    }

    @Test
    void testIsValidRequestVerificationToken() throws BaseException {
        String tempToken = createTestToken("testUser", Map.of("sub", "testUser"));
        when(cacheRepository.existsByKey("temp_testUser")).thenReturn(true);
        when(cacheRepository.findByKey("temp_testUser")).thenReturn("rvToken");
        assertTrue(jwtService.isValidRequestVerificationToken(tempToken, "rvToken"));
    }

    @Test
    void testIsValidRequestVerificationToken_NullExistingRvToken() throws BaseException {
        String tempToken = createTestToken("testUser", Map.of("sub", "testUser"));
        when(cacheRepository.existsByKey("temp_testUser")).thenReturn(true);
        when(cacheRepository.findByKey("temp_testUser")).thenReturn(null);

        assertThrows(BaseException.class, () -> jwtService.isValidRequestVerificationToken(tempToken, "rvToken"));
    }

    @Test
    void testIsValidRequestVerificationToken_RuntimeException() throws BaseException {
        String tempToken = createTestToken("testUser", Map.of("sub", "testUser"));
        when(cacheRepository.existsByKey("temp_testUser")).thenThrow(new RuntimeException("Test Exception"));
        assertThrows(BaseException.class, () -> jwtService.isValidRequestVerificationToken(tempToken, "rvToken"));
    }

    @Test
    void testIsValidRequestVerificationTokenUsingAccessToken() throws BaseException {
        String accessToken = createTestToken("testUser", Map.of("tType", "access"));
        when(cacheRepository.existsByKey("ac_testUser")).thenReturn(true);
        when(cacheRepository.findByKey("ac_testUser")).thenReturn("rvToken");
        assertTrue(jwtService.isValidRequestVerificationTokenUsingAccessToken(accessToken, "rvToken"));
    }

    @Test
    void testIsValidRequestVerificationTokenUsingAccessToken_NullExistingRvToken() throws BaseException {
        String accessToken = createTestToken("testUser", Map.of("tType", "access"));
        when(cacheRepository.existsByKey("ac_testUser")).thenReturn(true);
        when(cacheRepository.findByKey("ac_testUser")).thenReturn(null);

        assertThrows(BaseException.class, () -> jwtService.isValidRequestVerificationTokenUsingAccessToken(accessToken, "rvToken"));
    }

    @Test
    void testIsValidRequestVerificationTokenUsingAccessToken_ExceptionThrown() throws BaseException {
        String accessToken = createTestToken("testUser", Map.of("tType", "access"));
        when(cacheRepository.existsByKey("ac_testUser")).thenThrow(new RuntimeException("Test Excpetion"));

        assertThrows(BaseException.class, () -> jwtService.isValidRequestVerificationTokenUsingAccessToken(accessToken, "rvToken"));
    }

    private String createTestToken(String subject, Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpireTime))
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
                .compact();
    }
}