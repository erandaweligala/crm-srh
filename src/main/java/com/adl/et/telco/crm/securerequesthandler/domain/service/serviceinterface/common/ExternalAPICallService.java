package com.adl.et.telco.crm.securerequesthandler.domain.service.serviceinterface.common;

import com.adl.et.telco.crm.securerequesthandler.application.util.exception.BaseException;
import org.json.simple.parser.ParseException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface ExternalAPICallService {
    String externalGetAPICall(String url, Map<String, String> requestParam, Map<String, String> requestHeader) throws ParseException;

    String externalPostAPICall(String url, Object request,Map<String, String> requestParam, Map<String, String> requestHeader) throws  ParseException;

    String externalPutAPICall(String url, Object request,Map<String, String> valueMap, Map<String, String> requestHeader) throws  ParseException;

    String externalPatchAPICall(String url, Object request, Map<String, String> valueMap, Map<String, String> requestHeader) throws BaseException, ParseException;

    String externalDeleteAPICall(String url, Object request, Map<String, String> valueMap, Map<String, String> requestHeader) throws  ParseException;

    InputStream externalGetFileAsAStreamForPostEndPoint(String url, Object request, Map<String, String> valueMap, Map<String, String> requestHeader) throws IOException;

    String externalPostUploadAPICall(String url, MultipartFile multipartFile, Map<String, String> valueMap, Map<String, String> requestHeader) throws IOException,  ParseException;
}
