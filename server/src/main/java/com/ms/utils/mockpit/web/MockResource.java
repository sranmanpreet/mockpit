package com.ms.utils.mockpit.web;

import com.ms.utils.mockpit.aop.exception.MockNotFoundException;
import com.ms.utils.mockpit.aop.exception.MockpitApplicationException;
import com.ms.utils.mockpit.aop.interceptor.LogExecutionTime;
import com.ms.utils.mockpit.auth.AuthConfigCodec;
import com.ms.utils.mockpit.auth.AuthValidationResult;
import com.ms.utils.mockpit.auth.AuthValidator;
import com.ms.utils.mockpit.auth.AuthValidatorRegistry;
import com.ms.utils.mockpit.auth.SyntheticAuthRequest;
import com.ms.utils.mockpit.auth.config.AuthConfig;
import com.ms.utils.mockpit.domain.AuthType;
import com.ms.utils.mockpit.domain.Mock;
import com.ms.utils.mockpit.dto.MockDTO;
import com.ms.utils.mockpit.dto.MockResponse;
import com.ms.utils.mockpit.repository.MockRepository;
import com.ms.utils.mockpit.security.CurrentUser;
import com.ms.utils.mockpit.security.JwtPrincipal;
import com.ms.utils.mockpit.service.MockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/native/api/mocks")
public class MockResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockResource.class);

    @Autowired private MockService mockService;
    @Autowired private MockRepository mockRepository;
    @Autowired private AuthConfigCodec authConfigCodec;
    @Autowired private AuthValidatorRegistry authValidatorRegistry;

    @Value("${mockpit.quota.max-mocks-per-user:500}")
    private int maxMocksPerUser;

    @LogExecutionTime
    @GetMapping
    public ResponseEntity<MockResponse> getAllMocks(Pageable pageable) {
        Page<MockDTO> mockDTOPage = mockService.getAllMocks(pageable);
        return new ResponseEntity<>(createResponse(mockDTOPage), HttpStatus.OK);
    }

    @LogExecutionTime
    @PostMapping
    @Transactional
    public ResponseEntity<MockResponse> saveMock(@RequestBody MockDTO mock)
            throws MockpitApplicationException, MockNotFoundException {
        if (Objects.nonNull(mock.getId())) {
            MockDTO updatedMock = mockService.updateMock(mock);
            return new ResponseEntity<>(createResponse(updatedMock), HttpStatus.OK);
        }
        enforceQuotaForCreate();
        MockDTO savedMock = mockService.createMock(mock);
        return new ResponseEntity<>(createResponse(savedMock), HttpStatus.CREATED);
    }

    @LogExecutionTime
    @GetMapping("/{id}")
    public ResponseEntity<MockResponse> getMockById(@PathVariable Long id) throws MockNotFoundException {
        return new ResponseEntity<>(createResponse(mockService.getMockById(id)), HttpStatus.OK);
    }

    @LogExecutionTime
    @DeleteMapping("/{id}")
    public ResponseEntity<MockResponse> deleteMock(@PathVariable Long id) throws MockNotFoundException {
        mockService.deleteMockById(id);
        return new ResponseEntity<>(createResponse("Mock deleted", null), HttpStatus.OK);
    }

    @LogExecutionTime
    @DeleteMapping
    public ResponseEntity<MockResponse> deleteAllMocks() {
        mockService.deleteAllMocks();
        return new ResponseEntity<>(createResponse("All mocks deleted", null), HttpStatus.OK);
    }

    @LogExecutionTime
    @GetMapping("/search")
    public ResponseEntity<MockResponse> search(@RequestParam String query, Pageable pageable) {
        return new ResponseEntity<>(
                createResponse("Search results for '" + query + "'", mockService.performSearch(query, pageable)),
                HttpStatus.OK);
    }

    /**
     * Try a sample request against the mock's configured authentication scheme without invoking
     * the real mock dispatcher. Used by the front-end "Test auth" button. The synthetic request
     * carries only the headers supplied by the caller; this is intentional - we do not want to
     * accept arbitrary path/query data for a side-channel-free dry-run.
     */
    @PostMapping("/{id}/auth/test")
    public ResponseEntity<Map<String, Object>> testAuth(@PathVariable Long id,
                                                        @RequestBody AuthTestRequest sample) throws MockNotFoundException {
        Mock mock = mockService.getMockEntityById(id);
        AuthType type = mock.getAuthType();
        Map<String, Object> result = new HashMap<>();
        if (type == null || type == AuthType.NONE) {
            result.put("outcome", "SUCCESS");
            result.put("reason", "Mock has no authentication configured.");
            return ResponseEntity.ok(result);
        }
        AuthValidator validator = authValidatorRegistry.forType(type);
        AuthConfig cfg = authConfigCodec.decodeFromStorage(mock.getAuthConfigJson());

        SyntheticAuthRequest req = new SyntheticAuthRequest(
                mock.getRoute() == null ? "GET" : mock.getRoute().getMethod(),
                mock.getRoute() == null ? "/" : mock.getRoute().getPath(),
                sample == null ? null : sample.headers);
        AuthValidationResult res = validator.validate(req, cfg);
        result.put("outcome", res.isSuccess() ? "SUCCESS" : "FAILURE");
        if (res.isFailure()) {
            result.put("status", res.getSuggestedStatus() == null ? 401 : res.getSuggestedStatus().value());
            result.put("wwwAuthenticate", res.getWwwAuthenticate());
            result.put("reason", res.getReason());
        }
        return ResponseEntity.ok(result);
    }

    private void enforceQuotaForCreate() throws MockpitApplicationException {
        JwtPrincipal p = CurrentUser.get().orElse(null);
        if (p == null || p.isAdmin()) return;
        long count = mockRepository.countByUserId(p.getUserId());
        if (count >= maxMocksPerUser) {
            throw new MockpitApplicationException("Per-user mock quota reached (" + maxMocksPerUser + ").");
        }
    }

    private MockResponse createResponse(String message, Object data) { return new MockResponse(message, data); }
    private MockResponse createResponse(Object data) { return createResponse("", data); }

    public static class AuthTestRequest {
        public Map<String, String> headers;
        public String body;
    }
}
