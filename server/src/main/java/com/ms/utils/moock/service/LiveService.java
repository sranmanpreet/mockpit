package com.ms.utils.moock.service;

import com.ms.utils.moock.aop.exception.MockNotFoundException;
import com.ms.utils.moock.dto.LiveResponseDTO;
import com.ms.utils.moock.dto.MockDTO;
import com.ms.utils.moock.mapper.MockDTOLiveResponseDTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LiveService {

    @Autowired
    private MockService mockService;

    @Autowired
    private MockDTOLiveResponseDTOMapper mockDtoToLiveResponseMapper;

    public LiveResponseDTO getLiveResponse(String route, String method) throws MockNotFoundException {
        MockDTO mockDto = mockService.getMockByRouteAndMethod(route, method);
        return mockDtoToLiveResponseMapper.toLiveResponseDTO(mockDto);
    }
}
