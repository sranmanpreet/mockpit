package com.ms.utils.mockpit.service;

import com.ms.utils.mockpit.aop.exception.MockNotFoundException;
import com.ms.utils.mockpit.dto.LiveResponseDTO;
import com.ms.utils.mockpit.dto.MockDTO;
import com.ms.utils.mockpit.mapper.MockDTOLiveResponseDTOMapper;
import com.ms.utils.mockpit.web.LiveResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;

@Service
public class LiveService {

    private final Logger LOGGER = LoggerFactory.getLogger(LiveService.class);

    @Autowired
    private MockService mockService;

    @Autowired
    private MockDTOLiveResponseDTOMapper mockDtoToLiveResponseMapper;

    @Autowired
    private JavaScriptExecutionService jsExecutionService;

    public LiveResponseDTO getLiveResponse(HttpServletRequest request, String method) {
        String route = request.getRequestURI();
        MockDTO mockDto = mockService.getMockByRouteAndMethod(route, method);
        if(Objects.isNull(mockDto)){
            return null;
        }
        executeDynamicResponseBody(mockDto, request.getParameterMap());
        return mockDtoToLiveResponseMapper.toLiveResponseDTO(mockDto);
    }

    private void executeDynamicResponseBody(MockDTO mockDTO, Map<String, String[]> queryParams){
        if(mockDTO.getResponseBody().getType().equalsIgnoreCase("JAVASCRIPT")){
            mockDTO.getResponseBody().setContent(jsExecutionService.execute(mockDTO.getResponseBody().getContent(), queryParams));
        }
    }
}
