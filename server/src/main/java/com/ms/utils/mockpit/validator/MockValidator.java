package com.ms.utils.mockpit.validator;

import com.ms.utils.mockpit.aop.exception.MockNotFoundException;
import com.ms.utils.mockpit.aop.exception.MockpitApplicationException;
import com.ms.utils.mockpit.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class MockValidator {

    public boolean isMockValid(MockDTO mockDTO) throws MockpitApplicationException{
        if(Objects.isNull(mockDTO)){
            throw new MockpitApplicationException("Mock details not provided.");
        }
        if(Objects.isNull(mockDTO.getName()) || mockDTO.getName().isEmpty()){
            throw new MockpitApplicationException("Mock name not provided.");
        }
        return isResponseBodyValid(mockDTO.getResponseBody())
                && isResponseHeaderValid(mockDTO.getResponseHeaders())
                && isRouteValid(mockDTO.getRoute())
                && isResponseStatusValid(mockDTO.getResponseStatus());
    }

    private boolean isResponseBodyValid(ResponseBodyDTO responseBodyDTO) throws MockpitApplicationException {
        if(Objects.isNull(responseBodyDTO)){
            throw new MockpitApplicationException("Response body not provided.");
        }
        return true;
    }

    private boolean isResponseHeaderValid(List<ResponseHeaderDTO> responseHeaders) throws MockpitApplicationException {
        if(Objects.nonNull(responseHeaders)){
            for (ResponseHeaderDTO responseHeader : responseHeaders) {
                if(Objects.isNull(responseHeader.getName()) || Objects.isNull(responseHeader.getValue())){
                    throw new MockpitApplicationException("Invalid response headers.");
                }
            }
        }
        return true;
    }

    private boolean isRouteValid(RouteDTO routeDTO) throws MockpitApplicationException {
        if(Objects.isNull(routeDTO) || Objects.isNull(routeDTO.getMethod()) || Objects.isNull(routeDTO.getPath())){
            throw new MockpitApplicationException("Route details not provided.");
        }
        return true;
    }

    private boolean isResponseStatusValid(ResponseStatusDTO responseStatusDTO) throws MockpitApplicationException {
        if(Objects.isNull(responseStatusDTO) || Objects.isNull(responseStatusDTO.getCode())){
            throw new MockpitApplicationException("Response status code not provided.");
        }
        return true;
    }
}
