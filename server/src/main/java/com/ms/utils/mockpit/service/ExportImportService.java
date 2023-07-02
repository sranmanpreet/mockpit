package com.ms.utils.mockpit.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.utils.mockpit.aop.exception.MockpitApplicationException;
import com.ms.utils.mockpit.domain.Mock;
import com.ms.utils.mockpit.dto.ExportMockDTO;
import com.ms.utils.mockpit.dto.MockDTO;
import com.ms.utils.mockpit.mapper.ExportMockMapper;
import com.ms.utils.mockpit.mapper.MockMapper;
import com.ms.utils.mockpit.repository.MockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExportImportService {

    @Autowired
    private MockService mockService;

    @Autowired
    private MockMapper mockMapper;

    @Autowired
    private ExportMockMapper exportMockMapper;

    @Autowired
    private MockRepository mockRepository;

    public List<ExportMockDTO> getAllMocksForExport() {
        List<Mock> mocks = mockRepository.findAll();
        return exportMockMapper.toDTOList(mocks);
    }

    public byte[] exportMocks() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<ExportMockDTO> mockDtos = getAllMocksForExport();
        return mapper.writeValueAsBytes(mockDtos);
    }

    public void importMocks(MultipartFile file) throws MockpitApplicationException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<MockDTO> mockDTOs = objectMapper.readValue(file.getBytes(), new TypeReference<List<MockDTO>>() {});
        for (MockDTO mockDTO : mockDTOs) {
            mockService.createMock(mockDTO);
        }
    }
}
