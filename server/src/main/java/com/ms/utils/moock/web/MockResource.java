package com.ms.utils.moock.web;

import com.ms.utils.moock.aop.exception.MockNotFoundException;
import com.ms.utils.moock.aop.exception.MoockApplicationException;
import com.ms.utils.moock.domain.Mock;
import com.ms.utils.moock.dto.MockDTO;
import com.ms.utils.moock.dto.MockResponse;
import com.ms.utils.moock.service.MockService;
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

    @GetMapping
    public ResponseEntity<MockResponse> getAllMock() {
        List<MockDTO> mocks = mockService.getAllMocks();
        return new ResponseEntity<MockResponse>(createResponse(mocks), HttpStatus.OK);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<MockResponse> createMock(@RequestBody MockDTO mock) throws MoockApplicationException {
        MockDTO savedMock = mockService.createMock(mock);
        return new ResponseEntity<>(createResponse(savedMock), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MockResponse> getMockById(@PathVariable Long id) throws MockNotFoundException {
        LOGGER.info("Request to get Mock with id : {}", id);
        MockDTO mock = mockService.getMockById(id);
        return new ResponseEntity<MockResponse>(createResponse(mock), HttpStatus.OK);
    }

    @PutMapping
    @Transactional
    public ResponseEntity<MockResponse> updateMock(@RequestBody MockDTO mock) throws MockNotFoundException, MoockApplicationException {
        if(Objects.isNull(mock.getId())){
            throw new MockNotFoundException("Please provide Mock Id for the mock you want to update.");
        }
        MockDTO updatedMock = mockService.updateMock(mock);
        return new ResponseEntity<>(createResponse(updatedMock), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MockResponse> deleteMock(@PathVariable Long id) throws MockNotFoundException {
        mockService.deleteMockById(id);
        return new ResponseEntity<>(createResponse("Mock deleted", null), HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<MockResponse> deleteMock() {
        mockService.deleteAllMocks();
        return new ResponseEntity<>(createResponse("All mocks deleted", null), HttpStatus.OK);
    }

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
