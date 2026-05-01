package com.ms.utils.mockpit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.utils.mockpit.aop.exception.MockNotFoundException;
import com.ms.utils.mockpit.aop.exception.MockpitApplicationException;
import com.ms.utils.mockpit.auth.AuthConfigCodec;
import com.ms.utils.mockpit.auth.config.AuthConfig;
import com.ms.utils.mockpit.auth.config.AuthFailureResponse;
import com.ms.utils.mockpit.auth.config.NoneAuthConfig;
import com.ms.utils.mockpit.domain.AuthType;
import com.ms.utils.mockpit.domain.Mock;
import com.ms.utils.mockpit.domain.ResponseHeader;
import com.ms.utils.mockpit.dto.MockDTO;
import com.ms.utils.mockpit.dto.ResponseHeaderDTO;
import com.ms.utils.mockpit.dto.RouteDTO;
import com.ms.utils.mockpit.mapper.MockMapper;
import com.ms.utils.mockpit.mapper.ResponseBodyMapper;
import com.ms.utils.mockpit.mapper.ResponseHeaderMapper;
import com.ms.utils.mockpit.mapper.ResponseStatusMapper;
import com.ms.utils.mockpit.mapper.RouteMapper;
import com.ms.utils.mockpit.repository.MockRepository;
import com.ms.utils.mockpit.security.CurrentUser;
import com.ms.utils.mockpit.security.JwtPrincipal;
import com.ms.utils.mockpit.validator.MockValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class MockService {

    private static final int MAX_SEARCH_QUERY_LENGTH = 100;

    @Autowired private MockRepository mockRepository;
    @Autowired private MockMapper mockMapper;
    @Autowired private RouteMapper routeMapper;
    @Autowired private ResponseHeaderMapper responseHeaderMapper;
    @Autowired private ResponseBodyMapper responseBodyMapper;
    @Autowired private ResponseStatusMapper responseStatusMapper;
    @Autowired private MockValidator mockValidator;
    @Autowired private AuthConfigCodec authConfigCodec;
    @Autowired private ObjectMapper objectMapper;

    public Page<MockDTO> getAllMocks(Pageable pageable) {
        Optional<JwtPrincipal> p = CurrentUser.get();
        Page<Mock> mockPage;
        if (p.isPresent() && !p.get().isAdmin()) {
            mockPage = mockRepository.findAllByUserId(p.get().getUserId(), pageable);
        } else {
            mockPage = mockRepository.findAll(pageable);
        }
        return mockPage.map(this::toEnrichedDto);
    }

    public MockDTO createMock(MockDTO mockDTO) throws MockpitApplicationException {
        if (!mockValidator.isMockValid(mockDTO)) {
            return null;
        }
        Mock mock = new Mock();
        mock.setName(mockDTO.getName());
        mock.setDescription(mockDTO.getDescription());
        mock.setInactive(Boolean.TRUE.equals(mockDTO.getInactive()));
        CurrentUser.get().ifPresent(u -> mock.setUserId(u.getUserId()));

        mock.setResponseBody(responseBodyMapper.toEntity(mockDTO.getResponseBody()));
        RouteDTO route = mockDTO.getRoute();
        if (!route.getPath().startsWith("/")) {
            route.setPath("/" + route.getPath());
        }
        mock.setRoute(routeMapper.toEntity(route));
        mock.setResponseStatus(responseStatusMapper.toEntity(mockDTO.getResponseStatus()));
        mock.setResponseHeaders(responseHeaderMapper.toEntityList(mockDTO.getResponseHeaders()));

        applyAuthConfig(mock, mockDTO);

        return toEnrichedDto(mockRepository.save(mock));
    }

    public MockDTO updateMock(MockDTO mockDTO) throws MockNotFoundException, MockpitApplicationException {
        mockValidator.isMockValid(mockDTO);
        Mock existingMock = loadOwned(mockDTO.getId());

        existingMock.setName(mockDTO.getName());
        existingMock.setDescription(mockDTO.getDescription());
        existingMock.setInactive(Boolean.TRUE.equals(mockDTO.getInactive()));
        RouteDTO route = mockDTO.getRoute();
        if (!route.getPath().startsWith("/")) {
            route.setPath("/" + route.getPath());
        }
        existingMock.setRoute(routeMapper.toEntity(route));
        existingMock.setResponseBody(responseBodyMapper.toEntity(mockDTO.getResponseBody()));
        existingMock.setResponseStatus(responseStatusMapper.toEntity(mockDTO.getResponseStatus()));
        updateResponseHeaders(existingMock, mockDTO.getResponseHeaders());

        applyAuthConfig(existingMock, mockDTO);

        return toEnrichedDto(mockRepository.save(existingMock));
    }

    /**
     * Reads {@code authConfigRaw} (or {@code authConfig}) off the incoming DTO, runs it through the
     * {@link AuthConfigCodec} (which encrypts secrets and BCrypts passwords), and persists both the
     * resulting JSON blob and the optional auth-failure response on the entity.
     */
    private void applyAuthConfig(Mock entity, MockDTO dto) throws MockpitApplicationException {
        AuthConfig parsed;
        if (dto.getAuthConfigRaw() != null && !dto.getAuthConfigRaw().isNull()) {
            try {
                parsed = objectMapper.treeToValue(dto.getAuthConfigRaw(), AuthConfig.class);
            } catch (Exception ex) {
                throw new MockpitApplicationException("Invalid authConfig payload: "
                        + (ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage()));
            }
        } else if (dto.getAuthConfig() != null) {
            parsed = dto.getAuthConfig();
        } else {
            parsed = new NoneAuthConfig();
        }
        if (parsed == null || parsed.getType() == null || parsed.getType() == AuthType.NONE) {
            entity.setAuthType(AuthType.NONE);
            entity.setAuthConfigJson(null);
        } else {
            entity.setAuthType(parsed.getType());
            entity.setAuthConfigJson(authConfigCodec.encodeForStorage(parsed));
        }
        AuthFailureResponse fail = dto.getAuthFailure();
        if (fail == null) {
            entity.setAuthFailureStatus(null);
            entity.setAuthFailureBody(null);
            entity.setAuthFailureContentType(null);
        } else {
            entity.setAuthFailureStatus(fail.getStatus());
            entity.setAuthFailureBody(fail.getBody());
            entity.setAuthFailureContentType(fail.getContentType());
        }
    }

    /**
     * Wraps {@link MockMapper#toDto} and additionally surfaces the redacted auth config and the
     * configured auth-failure response so the UI can re-render them without round-tripping secrets.
     */
    private MockDTO toEnrichedDto(Mock mock) {
        MockDTO dto = mockMapper.toDto(mock);
        dto.setUserId(mock.getUserId());
        if (mock.getAuthType() != null && mock.getAuthType() != AuthType.NONE
                && mock.getAuthConfigJson() != null) {
            AuthConfig cfg = authConfigCodec.decodeFromStorage(mock.getAuthConfigJson());
            dto.setAuthConfig(authConfigCodec.redactForResponse(cfg));
        } else {
            dto.setAuthConfig(new NoneAuthConfig());
        }
        if (mock.getAuthFailureStatus() != null
                || mock.getAuthFailureBody() != null
                || mock.getAuthFailureContentType() != null) {
            dto.setAuthFailure(new AuthFailureResponse(
                    mock.getAuthFailureStatus(),
                    mock.getAuthFailureBody(),
                    mock.getAuthFailureContentType()));
        }
        return dto;
    }

    private void updateResponseHeaders(Mock mock, List<ResponseHeaderDTO> responseHeaderDTOs) {
        mock.getResponseHeaders().clear();
        if (responseHeaderDTOs != null) {
            for (ResponseHeaderDTO dto : responseHeaderDTOs) {
                ResponseHeader header = new ResponseHeader();
                header.setName(dto.getName());
                header.setValue(dto.getValue());
                header.setMockId(mock.getId());
                mock.getResponseHeaders().add(header);
            }
        }
    }

    public MockDTO getMockById(Long id) throws MockNotFoundException {
        return toEnrichedDto(loadOwned(id));
    }

    public Mock getMockEntityById(Long id) throws MockNotFoundException {
        return loadOwned(id);
    }

    public MockDTO getMockByRouteAndMethod(String route, String method) {
        List<Mock> mocks = mockRepository.findByRouteAndMethod(route, method);
        if (Objects.isNull(mocks) || mocks.isEmpty()) {
            return null;
        }
        return toEnrichedDto(mocks.get(mocks.size() - 1));
    }

    public List<MockDTO> getMocksByMethod(String method) {
        List<Mock> mocks = mockRepository.findByMethod(method);
        if (Objects.isNull(mocks) || mocks.isEmpty()) {
            return null;
        }
        return mocks.stream().map(this::toEnrichedDto).collect(java.util.stream.Collectors.toList());
    }

    public void deleteMockById(Long id) throws MockNotFoundException {
        Mock mock = loadOwned(id);
        mockRepository.delete(mock);
    }

    /**
     * Bulk delete now scoped to the current user. Admins still wipe everything; regular users only
     * delete their own mocks. The pre-2.0 "delete all mocks for everyone" behaviour was a footgun.
     */
    public void deleteAllMocks() {
        Optional<JwtPrincipal> p = CurrentUser.get();
        if (p.isPresent() && !p.get().isAdmin()) {
            mockRepository.findAllByUserId(p.get().getUserId(), Pageable.unpaged())
                    .forEach(mockRepository::delete);
        } else {
            mockRepository.deleteAll();
        }
    }

    public Page<MockDTO> performSearch(String query, Pageable pageable) {
        if (query == null) query = "";
        if (query.length() > MAX_SEARCH_QUERY_LENGTH) {
            query = query.substring(0, MAX_SEARCH_QUERY_LENGTH);
        }
        Optional<JwtPrincipal> p = CurrentUser.get();
        Page<Mock> page = (p.isPresent() && !p.get().isAdmin())
                ? mockRepository.searchMocksByUser(p.get().getUserId(), query, pageable)
                : mockRepository.searchMocks(query, pageable);
        return page.map(this::toEnrichedDto);
    }

    /**
     * Loads a mock by id, enforcing ownership. Throws {@link MockNotFoundException} if it does not
     * exist (so that probing for IDs you don't own returns the same response as probing for IDs
     * that don't exist - prevents IDOR-via-error-message).
     */
    private Mock loadOwned(Long id) throws MockNotFoundException {
        Optional<Mock> opt = mockRepository.findById(id);
        if (opt.isEmpty()) {
            throw new MockNotFoundException("Mock not found.");
        }
        Mock mock = opt.get();
        Optional<JwtPrincipal> p = CurrentUser.get();
        if (p.isPresent() && !p.get().isAdmin()) {
            if (mock.getUserId() == null || !mock.getUserId().equals(p.get().getUserId())) {
                throw new MockNotFoundException("Mock not found.");
            }
        }
        return mock;
    }

    /**
     * Used by access-denied paths that need to assert ownership without resolving the entity,
     * exposed so the controller layer can stay thin.
     */
    public void assertOwned(Long id) throws MockNotFoundException {
        loadOwned(id);
    }

    @SuppressWarnings("unused")
    private static AccessDeniedException accessDenied() {
        return new AccessDeniedException("Not authorised for this mock.");
    }
}
