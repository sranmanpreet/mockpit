package com.ms.utils.mockpit.service;

import com.ms.utils.mockpit.auth.AuthConfigCodec;
import com.ms.utils.mockpit.auth.AuthValidationResult;
import com.ms.utils.mockpit.auth.AuthValidator;
import com.ms.utils.mockpit.auth.AuthValidatorRegistry;
import com.ms.utils.mockpit.auth.config.AuthConfig;
import com.ms.utils.mockpit.domain.AuthType;
import com.ms.utils.mockpit.domain.Mock;
import com.ms.utils.mockpit.dto.LiveResponseDTO;
import com.ms.utils.mockpit.dto.MockDTO;
import com.ms.utils.mockpit.dto.ResponseHeaderDTO;
import com.ms.utils.mockpit.mapper.MockDTOLiveResponseDTOMapper;
import com.ms.utils.mockpit.mapper.MockMapper;
import com.ms.utils.mockpit.repository.MockRepository;
import com.ms.utils.mockpit.util.MockpitUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Resolves an inbound HTTP request to a configured mock and produces the live response. As of 2.0
 * the resolution path also enforces the per-mock authentication scheme: if the matched mock has
 * an {@code authType != NONE}, the corresponding {@code AuthValidator} runs before any dynamic JS
 * is executed, and a failure short-circuits with the user-configured failure response (or
 * RFC-compliant defaults).
 */
@Service
public class LiveService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiveService.class);

    @Autowired private MockRepository mockRepository;
    @Autowired private MockMapper mockMapper;
    @Autowired private MockDTOLiveResponseDTOMapper mockDtoToLiveResponseMapper;
    @Autowired private JavaScriptExecutionService jsExecutionService;
    @Autowired private AuthConfigCodec authConfigCodec;
    @Autowired private AuthValidatorRegistry validators;

    @Transactional(readOnly = true)
    public LiveResponseDTO getLiveResponse(HttpServletRequest request, String method) {
        String route = request.getRequestURI();
        Mock matched = findExact(route, method);
        if (matched == null) {
            matched = findByPathVariables(route, method);
        }
        if (matched == null) {
            return null;
        }
        AuthValidationResult auth = enforceAuth(request, matched);
        if (auth != null && auth.isFailure()) {
            return buildAuthFailureResponse(matched, auth);
        }

        MockDTO mockDto = mockMapper.toDto(matched);
        executeDynamicResponseBody(mockDto, request.getParameterMap(),
                MockpitUtil.getPathVariableMap(route, mockDto.getRoute().getPath()));
        return mockDtoToLiveResponseMapper.toLiveResponseDTO(mockDto);
    }

    private Mock findExact(String route, String method) {
        List<Mock> mocks = mockRepository.findByRouteAndMethod(route, method);
        if (mocks == null || mocks.isEmpty()) return null;
        return mocks.get(mocks.size() - 1);
    }

    private Mock findByPathVariables(String route, String method) {
        List<Mock> mocks = mockRepository.findByMethod(method);
        if (mocks == null || mocks.isEmpty()) return null;
        for (Mock m : mocks) {
            if (MockpitUtil.isMatch(route, m.getRoute().getPath())) return m;
        }
        return null;
    }

    private AuthValidationResult enforceAuth(HttpServletRequest request, Mock mock) {
        AuthType type = mock.getAuthType();
        if (type == null || type == AuthType.NONE) return null;
        AuthValidator validator = validators.forType(type);
        if (validator == null) return null;
        AuthConfig cfg = authConfigCodec.decodeFromStorage(mock.getAuthConfigJson());
        try {
            return validator.validate(request, cfg);
        } catch (RuntimeException ex) {
            LOGGER.warn("Auth validator {} threw {}", validator.getClass().getSimpleName(), ex.getClass().getSimpleName());
            return AuthValidationResult.failure(HttpStatus.INTERNAL_SERVER_ERROR, null,
                    "Auth validator failure.");
        }
    }

    private LiveResponseDTO buildAuthFailureResponse(Mock mock, AuthValidationResult auth) {
        Integer userStatus = mock.getAuthFailureStatus();
        String userBody = mock.getAuthFailureBody();
        String userType = mock.getAuthFailureContentType();

        int status = userStatus != null ? userStatus
                : (auth.getSuggestedStatus() != null ? auth.getSuggestedStatus().value() : 401);
        String body = userBody != null ? userBody : defaultErrorJson(auth);
        String contentType = userType != null ? userType : "application/json";

        List<ResponseHeaderDTO> headers = new ArrayList<>();
        if (auth.getWwwAuthenticate() != null) {
            ResponseHeaderDTO h = new ResponseHeaderDTO();
            h.setName(HttpHeaders.WWW_AUTHENTICATE);
            h.setValue(auth.getWwwAuthenticate());
            headers.add(h);
        }
        LiveResponseDTO out = new LiveResponseDTO();
        out.setStatusCode(status);
        out.setBody(body);
        out.setContentType(contentType);
        out.setHeaders(headers);
        return out;
    }

    private static String defaultErrorJson(AuthValidationResult auth) {
        String reason = auth.getReason() == null ? "Unauthorized" : auth.getReason()
                .replace("\\", "\\\\").replace("\"", "\\\"");
        return "{\"error\":\"unauthorized\",\"message\":\"" + reason + "\"}";
    }

    private void executeDynamicResponseBody(MockDTO mockDTO,
                                            Map<String, String[]> queryParams,
                                            Map<String, String> pathVariables) {
        if (mockDTO.getResponseBody() == null
                || mockDTO.getResponseBody().getType() == null
                || !mockDTO.getResponseBody().getType().equalsIgnoreCase("JAVASCRIPT")) {
            return;
        }
        Object executed = jsExecutionService.execute(
                mockDTO.getResponseBody().getContent(),
                queryParams == null ? Collections.emptyMap() : queryParams,
                pathVariables == null ? Collections.emptyMap() : pathVariables);
        mockDTO.getResponseBody().setContent(executed);
    }

    public void replaceContentTypeHeaderIfAny(LiveResponseDTO liveResponse) {
        List<ResponseHeaderDTO> headers = liveResponse.getHeaders();
        if (headers == null) return;
        for (ResponseHeaderDTO header : headers) {
            if (header.getName() != null && header.getName().equalsIgnoreCase("Content-Type")) {
                liveResponse.setContentType(header.getValue());
            }
        }
        if (liveResponse.getContentType() == null) {
            liveResponse.setContentType("application/json");
        }
    }

    @SuppressWarnings("unused")
    private static Object nonNull(Object o, Object fallback) { return Objects.isNull(o) ? fallback : o; }
}
