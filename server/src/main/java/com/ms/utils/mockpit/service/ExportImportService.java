package com.ms.utils.mockpit.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.utils.mockpit.aop.exception.MockpitApplicationException;
import com.ms.utils.mockpit.domain.Mock;
import com.ms.utils.mockpit.dto.ExportMockDTO;
import com.ms.utils.mockpit.dto.MockDTO;
import com.ms.utils.mockpit.mapper.ExportMockMapper;
import com.ms.utils.mockpit.mapper.MockMapper;
import com.ms.utils.mockpit.repository.MockRepository;
import com.ms.utils.mockpit.web.ExportImportResource;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExportImportService {

    private final Logger LOGGER = LoggerFactory.getLogger(ExportImportService.class);

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
        List<MockDTO> mockDTOs = null;
        try{
            mockDTOs = objectMapper.readValue(file.getBytes(), new TypeReference<List<MockDTO>>() {});
        } catch (JsonParseException ex){
            LOGGER.error("Error while parsing file.", ex);
            throw new MockpitApplicationException("Invalid file. Please upload a valid json.");
        } catch(SizeLimitExceededException ex){
            LOGGER.error("File size exceeded limit", ex);
            throw new MockpitApplicationException("File size too large. Maximum file size limit is 10MB.");
        }
        for (MockDTO mockDTO : mockDTOs) {
            mockService.createMock(mockDTO);
        }
    }
}
