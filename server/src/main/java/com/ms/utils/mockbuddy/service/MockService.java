package com.ms.utils.mockbuddy.service;

import com.ms.utils.mockbuddy.domain.Mock;
import com.ms.utils.mockbuddy.dto.MockDTO;
import com.ms.utils.mockbuddy.mapper.MockMapper;
import com.ms.utils.mockbuddy.repository.MockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class MockService {

    @Autowired
    private MockRepository mockRepository;

    @Autowired
    private MockMapper mockMapper;

    public List<MockDTO> getAllMocks() {
        List<Mock> mocks = mockRepository.findAll();
        return mockMapper.toDTOList(mocks);
    }
    @Transactional
    public MockDTO createMock(MockDTO mockDTO) {
        Mock mock = mockMapper.toEntity(mockDTO);
        mock = mockRepository.save(mock);
        return mockMapper.toDto(mock);
    }

    public MockDTO getMockById(Long id) {
        Mock mock = mockRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mock not found with id: " + id));
        return mockMapper.toDto(mock);
    }

    public MockDTO updateMock(Long id, MockDTO mockDTO) {
        Mock existingMock = mockRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mock not found with id: " + id));
        return mockMapper.toDto(existingMock);
    }

    public void deleteMockById(Long id) {
        mockRepository.deleteById(id);
    }
}
