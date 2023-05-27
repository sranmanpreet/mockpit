package com.ms.utils.moock.service;

import com.ms.utils.moock.domain.*;
import com.ms.utils.moock.dto.MockDTO;
import com.ms.utils.moock.dto.ResponseHeaderDTO;
import com.ms.utils.moock.mapper.*;
import com.ms.utils.moock.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.List;
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

    public List<MockDTO> getAllMocks() {
        List<Mock> mocks = mockRepository.findAll();
        return mockMapper.toDTOList(mocks);
    }
    @Transactional
    public MockDTO createMock(MockDTO mockDTO) {
        Mock mock = new Mock(mockDTO.getName(), mockDTO.getDescription());
        mock = mockRepository.save(mock);
        createAssociates(mock, mockDTO);
        return mockMapper.toDto(mock);
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

    public MockDTO getMockById(Long id) {
        Optional<Mock> mock = mockRepository.findById(id);
        if(mock.isPresent()){
            return mockMapper.toDto(mock.get());
        }
        return null;
    }

    public MockDTO getMockByRouteAndMethod(String route, String method) {
        List<Mock> mocks = mockRepository.findByRouteAndMethod(route, method);
        if(mocks.isEmpty()){
            return null;
        }
        return mockMapper.toDto(mocks.get(mocks.size()-1));
    }

    public MockDTO updateMock(Long id, MockDTO mockDTO) {
        Mock existingMock = mockRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Mock not found with id: " + id));
        return mockMapper.toDto(existingMock);
    }

    public void deleteMockById(Long id) {
        mockRepository.deleteById(id);
    }

    public void deleteAllMocks() { mockRepository.deleteAll(); }
}
