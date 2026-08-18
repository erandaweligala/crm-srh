package com.adl.et.telco.crm.securerequesthandler.application.filter.responsefilter;

import com.adl.et.telco.crm.securerequesthandler.domain.dto.authentication.PermissionDTO;
import com.adl.et.telco.crm.securerequesthandler.domain.dto.common.CommonNorthBoundResponse;
import com.adl.et.telco.crm.securerequesthandler.external.repository.cache.ActionRedisRepository;
import com.adl.et.telco.crm.securerequesthandler.domain.service.serviceimpl.access.JwtService;
import com.adl.et.telco.crm.securerequesthandler.application.util.ResponseHandler;
import com.adl.et.telco.crm.securerequesthandler.application.util.constants.Constants;
import com.adl.et.telco.crm.securerequesthandler.application.util.constants.ServiceConstants;
import com.adl.et.telco.crm.securerequesthandler.application.util.exception.BaseException;
import com.adl.et.telco.crm.securerequesthandler.application.util.resultenum.AuthCodeEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.adl.et.telco.crm.securerequesthandler.application.util.constants.ServiceConstants.PERMISSIONS;

/**
 * Filter for customizing response content based on user permissions.
 * Masks sensitive data in responses based on user's attribute permissions.
 */
@Component
@WebFilter(urlPatterns = "/*")
@Order(2)
@Slf4j
public class CustomResponseFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ResponseHandler responseHandler;
    private final ActionRedisRepository actionRedisRepository;
    private final ObjectMapper objectMapper;

    public CustomResponseFilter(JwtService jwtService, 
                              ResponseHandler responseHandler, 
                              ActionRedisRepository actionRedisRepository,
                              ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.responseHandler = responseHandler;
        this.actionRedisRepository = actionRedisRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            if (request.getHeader(ServiceConstants.AUTHORIZATION) == null) {
                filterChain.doFilter(request, response);
                return;
            }

            CustomResponseWrapper customResponseWrapper = new CustomResponseWrapper(response);
            filterChain.doFilter(request, customResponseWrapper);

            byte[] modifiedResponseBytes = processResponse(request, response, customResponseWrapper);
            response.setContentLength(modifiedResponseBytes.length);
            response.getOutputStream().write(modifiedResponseBytes);
        } catch (BaseException ex) {
            handleBaseException(response, ex);
        } catch (Exception ex) {
            handleGenericException(response, ex);
        } finally {
            ResponseFilterContextHolder.destroy();
        }
    }

    private byte[] processResponse(HttpServletRequest request, HttpServletResponse response,
                                 CustomResponseWrapper customResponseWrapper) throws IOException {
        byte[] modifiedResponseBytes = customResponseWrapper.getResponseAsByteArray();
        String modifiedResponse = new String(modifiedResponseBytes, StandardCharsets.UTF_8);

        if (ResponseFilterContextHolder.isFilterEnabled() && HttpStatus.valueOf(response.getStatus()).is2xxSuccessful()) {
            PermissionDTO permissions = getUserPermissions(request);
            modifiedResponse = filterApiResponse(modifiedResponse, permissions);
            modifiedResponseBytes = modifiedResponse.getBytes(StandardCharsets.UTF_8);
        }

        return modifiedResponseBytes;
    }

    private void handleBaseException(HttpServletResponse response, BaseException ex) throws IOException {
        CommonNorthBoundResponse<String> commonAdaptorResp = responseHandler.northBoundRespHandler(
            ex.getResultCode(), ex.getReason());
        writeErrorResponse(response, ex.getHttpStatus().value(), commonAdaptorResp);
    }

    private void handleGenericException(HttpServletResponse response, Exception ex) throws IOException {
        log.error("SRH|Unexpected error in response filter", ex);
        CommonNorthBoundResponse<String> commonAdaptorResp = responseHandler.northBoundRespHandler(
            AuthCodeEnum.INTERNAL_SERVER_ERROR.code(), 
            AuthCodeEnum.INTERNAL_SERVER_ERROR.description());
        writeErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR.value(), commonAdaptorResp);
    }

    private void writeErrorResponse(HttpServletResponse response, int status, 
                                  CommonNorthBoundResponse<String> errorResponse) throws IOException {
        response.setStatus(status);
        response.setHeader(ServiceConstants.CONTENT_TYPE, ServiceConstants.APPLICATION_JSON);
        String json = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(json);
    }

    private String filterApiResponse(String modifiedResponse, PermissionDTO permissions) {
        Map<Integer, String> controllableAttributeDetailsForAction = 
            actionRedisRepository.findByActionId(ResponseFilterContextHolder.getActionId());
            
        if (controllableAttributeDetailsForAction == null || controllableAttributeDetailsForAction.isEmpty()) {
            log.error("SRH|Action - Attribute Details Not Found In Redis");
            throw new BaseException(
                AuthCodeEnum.UNAUTHORIZED_AT_THE_MOMENT.description(),
                AuthCodeEnum.UNAUTHORIZED_AT_THE_MOMENT.description(),
                HttpStatus.UNAUTHORIZED,
                AuthCodeEnum.UNAUTHORIZED_AT_THE_MOMENT.code(),
                null
            );
        }

        List<Long> distinctAttributes = permissions.getComponents().stream()
            .flatMap(dto -> dto.getAttributes().stream())
            .collect(Collectors.toList());

        Map<String, String> updates = new HashMap<>();
        for (Map.Entry<Integer, String> entry : controllableAttributeDetailsForAction.entrySet()) {
            if (!distinctAttributes.contains((long) entry.getKey())) {
                updates.put(entry.getValue(), Constants.RESPONSE_FILTER_MASK);
            }
        }

        return updates.isEmpty() ? modifiedResponse : modifyJsonWithPath(modifiedResponse, updates);
    }

    private PermissionDTO getUserPermissions(HttpServletRequest servletRequest) {
        try {
            Claims allClaims = jwtService.extractAllClaims(jwtService.tokenExtractor(servletRequest));
            Object permissions = allClaims.get(PERMISSIONS);
            return objectMapper.convertValue(permissions, PermissionDTO.class);
        } catch (Exception e) {
            log.error("SRH|Error extracting user permissions", e);
            throw e;
        }
    }

    private String modifyJsonWithPath(String jsonString, Map<String, String> updates) {
        try {
            Configuration config = Configuration.defaultConfiguration().addOptions(Option.ALWAYS_RETURN_LIST);
            DocumentContext context = JsonPath.using(config).parse(jsonString);

            for (Map.Entry<String, String> entry : updates.entrySet()) {
                String path = entry.getKey();
                String maskedValue = entry.getValue();

                List<Object> matches = context.read(path);
                if (matches != null && !matches.isEmpty()) {
                    // Re-parse to get the original structure for replacements
                    Object original = JsonPath.using(config).parse(jsonString).read("$");

                    // Walk the document and find all match paths manually
                    List<String> allPaths = JsonPath.using(config).parse(jsonString).read(path, List.class);
                    for (int i = 0; i < matches.size(); i++) {
                        try {
                            // JsonPath.set() will update the path, even with filters
                            context.set(path, maskedValue); // overwrites all matches
                        } catch (Exception ex) {
                            log.warn("Could not mask value at path {}: {}", path, ex.getMessage());
                        }
                    }
                } else {
                    log.warn("No match found for JSONPath: {}", path);
                }
            }

            return context.jsonString();
        } catch (Exception e) {
            log.error("SRH|Error modifying JSON response", e);
            return jsonString;
        }
    }
}

