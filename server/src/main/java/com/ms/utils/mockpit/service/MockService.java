package com.ms.utils.mockpit.service;

import com.ms.utils.mockpit.aop.exception.MockNotFoundException;
import com.ms.utils.mockpit.aop.exception.MockpitApplicationException;
import com.ms.utils.mockpit.domain.*;
import com.ms.utils.mockpit.dto.ExportMockDTO;
import com.ms.utils.mockpit.dto.MockDTO;
import com.ms.utils.mockpit.dto.ResponseHeaderDTO;
import com.ms.utils.mockpit.dto.RouteDTO;
import com.ms.utils.mockpit.mapper.*;
import com.ms.utils.mockpit.repository.*;
import com.ms.utils.mockpit.validator.MockValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.*;

@Service
@Transactional
public class MockService {

    @Autowired
    private MockRepository mockRepository;

    @Autowired
    private ResponseHeaderRepository responseHeaderRepository;

    @Autowired
    private MockMapper mockMapper;

    @Autowired
    private RouteMapper routeMapper;

    @Autowired
    private ResponseHeaderMapper responseHeaderMapper;

    @Autowired
    private ResponseBodyMapper responseBodyMapper;

    @Autowired
    private ResponseStatusMapper responseStatusMapper;

    @Autowired
    private MockValidator mockValidator;

    public Page<MockDTO> getAllMocks(Pageable pageable) {
        Page<Mock> mockPage = mockRepository.findAll(pageable);

        return mockPage.map(mockMapper::toDto);
    }

    public MockDTO createMock(MockDTO mockDTO) throws MockpitApplicationException {
        if(mockValidator.isMockValid(mockDTO)){
            Mock mock = new Mock(mockDTO.getName(), mockDTO.getDescription());

            mock.setResponseBody(responseBodyMapper.toEntity(mockDTO.getResponseBody()));
            RouteDTO route = mockDTO.getRoute();
            if(!route.getPath().startsWith("/")){
                route.setPath("/"+route.getPath());
            }
            mock.setRoute(routeMapper.toEntity(route));
            mock.setResponseStatus(responseStatusMapper.toEntity(mockDTO.getResponseStatus()));
            mock.setResponseHeaders(responseHeaderMapper.toEntityList(mockDTO.getResponseHeaders()));
            mock = mockRepository.save(mock);
        //    createAssociates(mock, mockDTO);
            return mockMapper.toDto(mock);
        }
        return null;
    }

    public MockDTO updateMock(MockDTO mockDTO) throws MockNotFoundException, MockpitApplicationException {
        mockValidator.isMockValid(mockDTO);
        Mock existingMock = mockRepository.findById(mockDTO.getId())
                .orElseThrow(() -> new MockNotFoundException("Mock not found"));

        // Update fields of the existing mock
        existingMock.setName(mockDTO.getName());
        existingMock.setDescription(mockDTO.getDescription());
        RouteDTO route = mockDTO.getRoute();
        if(!route.getPath().startsWith("/")){
            route.setPath("/"+route.getPath());
        }
        existingMock.setRoute(routeMapper.toEntity(route));
        existingMock.setRoute(routeMapper.toEntity(mockDTO.getRoute()));
        existingMock.setResponseBody(responseBodyMapper.toEntity(mockDTO.getResponseBody()));
        existingMock.setResponseStatus(responseStatusMapper.toEntity(mockDTO.getResponseStatus()));
        updateResponseHeaders(existingMock, mockDTO.getResponseHeaders());
        return mockMapper.toDto(mockRepository.save(existingMock));
    }

    private void updateResponseHeaders(Mock mock, List<ResponseHeaderDTO> responseHeaderDTOs) {
        // Clear the existing ResponseHeaders to remove any orphaned entities
        mock.getResponseHeaders().clear();

        if (responseHeaderDTOs != null) {
            // Create new ResponseHeader entities and associate them with the Mock
            for (ResponseHeaderDTO responseHeaderDTO : responseHeaderDTOs) {
                ResponseHeader responseHeader = new ResponseHeader();
                responseHeader.setName(responseHeaderDTO.getName());
                responseHeader.setValue(responseHeaderDTO.getValue());
                responseHeader.setMockId(mock.getId());
                mock.getResponseHeaders().add(responseHeader);
            }
        }
    }

    public MockDTO getMockById(Long id) throws MockNotFoundException {
        Optional<Mock> mock = mockRepository.findById(id);
        if(!mock.isPresent()){
            throw new MockNotFoundException("Requeste" +
                    "d Mock not found");
        }
        return mockMapper.toDto(mock.get());
    }

    public MockDTO getMockByRouteAndMethod(String route, String method) {
        List<Mock> mocks = mockRepository.findByRouteAndMethod(route, method);
        if(Objects.isNull(mocks) || mocks.isEmpty()){
            return null;
        }
        return mockMapper.toDto(mocks.get(mocks.size()-1));
    }

    public List<MockDTO> getMocksByMethod(String method) {
        List<Mock> mocks = mockRepository.findByMethod(method);
        if(Objects.isNull(mocks) || mocks.isEmpty()){
            return null;
        }
        return mockMapper.toDTOList(mocks);
    }

    public void deleteMockById(Long id) throws MockNotFoundException {
        Optional<Mock> existingMock = mockRepository.findById(id);
        if(!existingMock.isPresent()){
            throw new MockNotFoundException("No Mock found with id "+ id);
        }
        mockRepository.deleteById(id);
    }

    public void deleteAllMocks() { mockRepository.deleteAll(); }

    public Page<MockDTO> performSearch(String query, Pageable pageable) {
        Page<Mock> mockPage = mockRepository.searchMocks(query, pageable);
        return mockPage.map(mockMapper::toDto);
    }
}
