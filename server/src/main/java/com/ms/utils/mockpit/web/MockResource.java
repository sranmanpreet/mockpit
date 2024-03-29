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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<MockResponse> getAllMocks(Pageable pageable) {
        Page<MockDTO> mockDTOPage = mockService.getAllMocks(pageable);
        return new ResponseEntity<MockResponse>(createResponse(mockDTOPage), HttpStatus.OK);
    }

    @LogExecutionTime
    @PostMapping
    @Transactional
    public ResponseEntity<MockResponse> saveMock(@RequestBody MockDTO mock) throws MockpitApplicationException, MockNotFoundException {
        if(Objects.nonNull(mock.getId())){
            MockDTO updatedMock = mockService.updateMock(mock);
            return new ResponseEntity<>(createResponse(updatedMock), HttpStatus.OK);
        } else {
            MockDTO savedMock = mockService.createMock(mock);
            return new ResponseEntity<>(createResponse(savedMock), HttpStatus.CREATED);
        }
    }

    @LogExecutionTime
    @GetMapping("/{id}")
    public ResponseEntity<MockResponse> getMockById(@PathVariable Long id) throws MockNotFoundException {
        LOGGER.info("Request to get Mock with id : {}", id);
        MockDTO mock = mockService.getMockById(id);
        return new ResponseEntity<MockResponse>(createResponse(mock), HttpStatus.OK);
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
    public ResponseEntity<MockResponse> search(@RequestParam String query, Pageable pageable) {
        return new ResponseEntity<>(createResponse("Search results for '"+ query + "'", mockService.performSearch(query, pageable)), HttpStatus.OK);
    }

    private MockResponse createResponse(String message, Object data){
        return new MockResponse(message, data);
    }

    private MockResponse createResponse(Object data){
        return createResponse("", data);
    }
}
