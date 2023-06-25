package com.ms.utils.moock.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.utils.moock.aop.exception.MoockApplicationException;
import com.ms.utils.moock.domain.Mock;
import com.ms.utils.moock.dto.ExportMockDTO;
import com.ms.utils.moock.dto.MockDTO;
import com.ms.utils.moock.mapper.ExportMockMapper;
import com.ms.utils.moock.mapper.MockMapper;
import com.ms.utils.moock.repository.MockRepository;
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

    public void importMocks(MultipartFile file) throws MoockApplicationException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<MockDTO> mockDTOs = objectMapper.readValue(file.getBytes(), new TypeReference<List<MockDTO>>() {});
        for (MockDTO mockDTO : mockDTOs) {
            mockService.createMock(mockDTO);
        }
    }
}
