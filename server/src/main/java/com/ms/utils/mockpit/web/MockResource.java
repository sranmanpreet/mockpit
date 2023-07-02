package com.ms.utils.mockpit.web;

import com.ms.utils.mockpit.aop.exception.MockNotFoundException;
import com.ms.utils.mockpit.aop.exception.MockpitApplicationException;
import com.ms.utils.mockpit.aop.interceptor.LogExecutionTime;
import com.ms.utils.mockpit.domain.Mock;
import com.ms.utils.mockpit.dto.MockDTO;
import com.ms.utils.mockpit.dto.MockResponse;
import com.ms.utils.mockpit.service.MockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/native/api/mocks")
public class MockResource {

    private final Logger LOGGER = LoggerFactory.getLogger(LiveResource.class);

    @Autowired
    private MockService mockService;

    @LogExecutionTime
    @GetMapping
    public ResponseEntity<MockResponse> getAllMock() {
        List<MockDTO> mocks = mockService.getAllMocks();
        return new ResponseEntity<MockResponse>(createResponse(mocks.size() + " Mocks found.",mocks), HttpStatus.OK);
    }

    @LogExecutionTime
    @GetMapping("/entities")
    public ResponseEntity<MockResponse> getAllMockEntities() {
        List<Mock> mocks = mockService.getAllMockEntities();
        return new ResponseEntity<MockResponse>(createResponse(mocks.size() + " Mocks found.",mocks), HttpStatus.OK);
    }

    @LogExecutionTime
    @PostMapping
    @Transactional
    public ResponseEntity<MockResponse> createMock(@RequestBody MockDTO mock) throws MockpitApplicationException {
        MockDTO savedMock = mockService.createMock(mock);
        return new ResponseEntity<>(createResponse(savedMock), HttpStatus.CREATED);
    }

    @LogExecutionTime
    @GetMapping("/{id}")
    public ResponseEntity<MockResponse> getMockById(@PathVariable Long id) throws MockNotFoundException {
        LOGGER.info("Request to get Mock with id : {}", id);
        MockDTO mock = mockService.getMockById(id);
        return new ResponseEntity<MockResponse>(createResponse(mock), HttpStatus.OK);
    }

    @LogExecutionTime
    @PutMapping
    @Transactional
    public ResponseEntity<MockResponse> updateMock(@RequestBody MockDTO mock) throws MockNotFoundException, MockpitApplicationException {
        if(Objects.isNull(mock.getId())){
            throw new MockNotFoundException("Please provide Mock Id for the mock you want to update.");
        }
        MockDTO updatedMock = mockService.updateMock(mock);
        return new ResponseEntity<>(createResponse(updatedMock), HttpStatus.OK);
    }

    @LogExecutionTime
    @DeleteMapping("/{id}")
    public ResponseEntity<MockResponse> deleteMock(@PathVariable Long id) throws MockNotFoundException {
        mockService.deleteMockById(id);
        return new ResponseEntity<>(createResponse("Mock deleted", null), HttpStatus.OK);
    }

    @LogExecutionTime
    @DeleteMapping
    public ResponseEntity<MockResponse> deleteMock() {
        mockService.deleteAllMocks();
        return new ResponseEntity<>(createResponse("All mocks deleted", null), HttpStatus.OK);
    }

    @LogExecutionTime
    @GetMapping("/search")
    public ResponseEntity<MockResponse> search(@RequestParam String query) {
        return new ResponseEntity<>(createResponse("Search results for '"+ query + "'", mockService.performSearch(query)), HttpStatus.OK);
    }

    private MockResponse createResponse(String message, Object data){
        return new MockResponse(message, data);
    }

    private MockResponse createResponse(Object data){
        return createResponse("", data);
    }
}
