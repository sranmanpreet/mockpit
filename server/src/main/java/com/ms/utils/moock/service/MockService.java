package com.ms.utils.moock.service;

import com.ms.utils.moock.aop.exception.MockNotFoundException;
import com.ms.utils.moock.aop.exception.MoockApplicationException;
import com.ms.utils.moock.domain.*;
import com.ms.utils.moock.dto.MockDTO;
import com.ms.utils.moock.mapper.*;
import com.ms.utils.moock.repository.*;
import com.ms.utils.moock.validator.MockValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class MockService {

    @Autowired
    private MockRepository mockRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private ResponseHeaderRepository responseHeaderRepository;

    @Autowired
    private ResponseBodyRepository responseBodyRepository;

    @Autowired
    private ResponseStatusRepository responseStatusRepository;

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

    public List<MockDTO> getAllMocks() {
        List<Mock> mocks = mockRepository.findAll();
        return mockMapper.toDTOList(mocks);
    }

    public MockDTO createMock(MockDTO mockDTO) throws MoockApplicationException {
        if(mockValidator.isMockValid(mockDTO)){
            Mock mock = new Mock(mockDTO.getName(), mockDTO.getDescription());
            mock = mockRepository.save(mock);
            createAssociates(mock, mockDTO);
            return mockMapper.toDto(mock);
        }
        return null;
    }

    private void createAssociates(Mock mock, MockDTO mockDTO) {
        createResponseBody(mock, mockDTO);
        createResponseHeaders(mock, mockDTO);
        createRoute(mock, mockDTO);
        createResponseStatus(mock, mockDTO);
    }

    private void createResponseStatus(Mock mock, MockDTO mockDTO) {
        ResponseStatus rs = responseStatusMapper.toEntity(mockDTO.getResponseStatus());
        rs.setMock(mock);
        rs = responseStatusRepository.save(rs);
        mock.setResponseStatus(rs);
    }

    private void createResponseBody(Mock mock, MockDTO mockDTO) {
        ResponseBody rb = responseBodyMapper.toEntity(mockDTO.getResponseBody());
        rb.setMock(mock);
        rb = responseBodyRepository.save(rb);
        mock.setResponseBody(rb);
    }

    private void createResponseHeaders(Mock mock, MockDTO mockDTO) {
        if(Objects.isNull(mockDTO.getResponseHeaders())){
            return;
        }
        Set<ResponseHeader> responseHeaders = responseHeaderMapper.toEntityList(mockDTO.getResponseHeaders());
        for (ResponseHeader responseHeader : responseHeaders) {
            responseHeader.setMock(mock);
        }
        List<ResponseHeader> headers = responseHeaderRepository.saveAll(responseHeaders);
        mock.setResponseHeaders(headers);
    }

    private void createRoute(Mock mock, MockDTO mockDTO) {
        Route route = routeMapper.toEntity(mockDTO.getRoute());
        route.setMock(mock);
        route = routeRepository.save(route);
        mock.setRoute(route);
    }

    public MockDTO getMockById(Long id) throws MockNotFoundException {
        Optional<Mock> mock = mockRepository.findById(id);
        if(!mock.isPresent()){
            throw new MockNotFoundException("No Mock found with id "+id);
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

    public MockDTO updateMock(MockDTO mockDTO) throws MockNotFoundException, MoockApplicationException {
        Optional<Mock> existingMock = mockRepository.findById(mockDTO.getId());
        if(!existingMock.isPresent()){
            throw new MockNotFoundException("No Mock found with id "+mockDTO.getId());
        }
        Mock updatedMock = null;
        if(mockValidator.isMockValid(mockDTO)){
            updatedMock =  mockRepository.save(mockMapper.toEntity(mockDTO));
        }
        return mockMapper.toDto(updatedMock);
    }

    public void deleteMockById(Long id) throws MockNotFoundException {
        Optional<Mock> existingMock = mockRepository.findById(id);
        if(!existingMock.isPresent()){
            throw new MockNotFoundException("No Mock found with id "+ id);
        }
        mockRepository.deleteById(id);
    }

    public void deleteAllMocks() { mockRepository.deleteAll(); }

    public List<MockDTO> performSearch(String query) {
        List<Mock> mocks = mockRepository.searchMocks(query);
        return mockMapper.toDTOList(mocks);
    }
}
