package com.ms.utils.moock.web;

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
    public ResponseEntity<List<MockDTO>> getAllMock() {
        List<MockDTO> mocks = mockService.getAllMocks();
        return new ResponseEntity<List<MockDTO>>(mocks, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<MockDTO> createMock(@RequestBody MockDTO mock) {
        MockDTO savedMock = mockService.createMock(mock);
        return new ResponseEntity<>(savedMock, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MockDTO> getMockById(@PathVariable Long id) {
        MockDTO mock = mockService.getMockById(id);
        return new ResponseEntity<MockDTO>(mock, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MockDTO> updateMock(@PathVariable Long id, @RequestBody MockDTO mock) {
        mock.setId(id);
        MockDTO updatedMock = mockService.updateMock(id, mock);
        return new ResponseEntity<>(updatedMock, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMock(@PathVariable Long id) {
        mockService.deleteMockById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
