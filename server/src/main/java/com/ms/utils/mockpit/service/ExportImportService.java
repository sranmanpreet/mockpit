package com.ms.utils.mockpit.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.utils.mockpit.aop.exception.MockpitApplicationException;
import com.ms.utils.mockpit.domain.Mock;
import com.ms.utils.mockpit.dto.ExportMockDTO;
import com.ms.utils.mockpit.dto.MockDTO;
import com.ms.utils.mockpit.mapper.ExportMockMapper;
import com.ms.utils.mockpit.repository.MockRepository;
import com.ms.utils.mockpit.security.CurrentUser;
import com.ms.utils.mockpit.security.JwtPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ExportImportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportImportService.class);

    @Autowired private MockService mockService;
    @Autowired private ExportMockMapper exportMockMapper;
    @Autowired private MockRepository mockRepository;
    @Autowired private ObjectMapper objectMapper;

    @Value("${mockpit.import.max-mock-count:1000}")
    private int maxMockImportCount;

    /**
     * Export only the mocks owned by the current user (admins export everything). The previous
     * behaviour - exporting all mocks regardless of caller - was a data-leak risk in a multi-tenant
     * deployment.
     */
    public List<ExportMockDTO> getAllMocksForExport() {
        Optional<JwtPrincipal> p = CurrentUser.get();
        List<Mock> mocks;
        if (p.isPresent() && !p.get().isAdmin()) {
            mocks = mockRepository.findAllByUserId(p.get().getUserId(), Pageable.unpaged()).getContent();
        } else {
            mocks = mockRepository.findAll();
        }
        return exportMockMapper.toDTOList(mocks);
    }

    public byte[] exportMocks() throws IOException {
        return objectMapper.writeValueAsBytes(getAllMocksForExport());
    }

    public void importMocks(MultipartFile file) throws MockpitApplicationException, IOException {
        if (file == null || file.isEmpty()) {
            throw new MockpitApplicationException("Import file is empty.");
        }
        List<MockDTO> mockDTOs;
        try {
            mockDTOs = objectMapper.readValue(file.getBytes(), new TypeReference<List<MockDTO>>() { });
        } catch (JsonParseException ex) {
            LOGGER.warn("Import rejected: invalid JSON.");
            throw new MockpitApplicationException("Invalid file. Please upload a valid mock JSON export.");
        }
        if (mockDTOs == null || mockDTOs.isEmpty()) {
            throw new MockpitApplicationException("Import file contained no mocks.");
        }
        if (mockDTOs.size() > maxMockImportCount) {
            throw new MockpitApplicationException("Import exceeds the maximum of " + maxMockImportCount + " mocks per request.");
        }
        for (MockDTO mockDTO : mockDTOs) {
            // IDs from another instance are ignored - the create flow assigns fresh ones to the
            // current user (so re-imports never overwrite existing mocks owned by someone else).
            mockDTO.setId(null);
            mockService.createMock(mockDTO);
        }
    }
}
