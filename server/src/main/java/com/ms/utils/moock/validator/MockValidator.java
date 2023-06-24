package com.ms.utils.moock.validator;

import com.ms.utils.moock.aop.exception.MockNotFoundException;
import com.ms.utils.moock.aop.exception.MoockApplicationException;
import com.ms.utils.moock.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class MockValidator {

    public boolean isMockValid(MockDTO mockDTO) throws MoockApplicationException{
        if(Objects.isNull(mockDTO)){
            throw new MoockApplicationException("Mock details not provided.");
        }
        if(Objects.isNull(mockDTO.getName()) || mockDTO.getName().isEmpty()){
            throw new MoockApplicationException("Mock name not provided.");
        }
        return isResponseBodyValid(mockDTO.getResponseBody())
                && isResponseHeaderValid(mockDTO.getResponseHeaders())
                && isRouteValid(mockDTO.getRoute())
                && isResponseStatusValid(mockDTO.getResponseStatus());
    }

    private boolean isResponseBodyValid(ResponseBodyDTO responseBodyDTO) throws MoockApplicationException {
        if(Objects.isNull(responseBodyDTO)){
            throw new MoockApplicationException("Response body not provided.");
        }
        return true;
    }

    private boolean isResponseHeaderValid(List<ResponseHeaderDTO> responseHeaders) throws MoockApplicationException {
        if(Objects.nonNull(responseHeaders)){
            for (ResponseHeaderDTO responseHeader : responseHeaders) {
                if(Objects.isNull(responseHeader.getName()) || Objects.isNull(responseHeader.getValue())){
                    throw new MoockApplicationException("Invalid response headers.");
                }
            }
        }
        return true;
    }

    private boolean isRouteValid(RouteDTO routeDTO) throws MoockApplicationException {
        if(Objects.isNull(routeDTO) || Objects.isNull(routeDTO.getMethod()) || Objects.isNull(routeDTO.getPath())){
            throw new MoockApplicationException("Route details not provided.");
        }
        return true;
    }

    private boolean isResponseStatusValid(ResponseStatusDTO responseStatusDTO) throws MoockApplicationException {
        if(Objects.isNull(responseStatusDTO) || Objects.isNull(responseStatusDTO.getCode())){
            throw new MoockApplicationException("Response status code not provided.");
        }
        return true;
    }
}
