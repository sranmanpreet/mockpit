package com.ms.utils.moock.web;

import com.ms.utils.moock.aop.exception.MockNotFoundException;
import com.ms.utils.moock.aop.exception.MoockApplicationException;
import com.ms.utils.moock.dto.MockDTO;
import com.ms.utils.moock.service.MockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/native/api/mocks")
public class MockResource {

    @Autowired
    private MockService mockService;

    @GetMapping
    public ResponseEntity<List<MockDTO>> getAllMock() throws MoockApplicationException {
        List<MockDTO> mocks = mockService.getAllMocks();
        return new ResponseEntity<List<MockDTO>>(mocks, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<MockDTO> createMock(@RequestBody MockDTO mock) throws MoockApplicationException {
        MockDTO savedMock = mockService.createMock(mock);
        return new ResponseEntity<>(savedMock, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MockDTO> getMockById(@PathVariable Long id) {
        MockDTO mock = mockService.getMockById(id);
        return new ResponseEntity<MockDTO>(mock, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MockDTO> updateMock(@PathVariable Long id, @RequestBody MockDTO mock) throws MockNotFoundException {
        mock.setId(id);
        MockDTO updatedMock = mockService.updateMock(id, mock);
        return new ResponseEntity<>(updatedMock, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteMock(@PathVariable Long id) throws MockNotFoundException {
        mockService.deleteMockById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Mock with ID : ${id} deleted");
    }

    @DeleteMapping
    public ResponseEntity<Object> deleteMock() {
        mockService.deleteAllMocks();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("All mocks deleted");
    }
}
