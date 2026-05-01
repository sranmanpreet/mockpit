package com.ms.utils.mockpit.validator;

import com.ms.utils.mockpit.aop.exception.MockpitApplicationException;
import com.ms.utils.mockpit.dto.*;
import com.ms.utils.mockpit.web.filter.ReservedPathFilter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class MockValidator {

    private static final int MAX_NAME_LENGTH = 255;
    private static final int MAX_DESCRIPTION_LENGTH = 1000;
    private static final int MAX_PATH_LENGTH = 1000;
    private static final int MAX_HEADER_NAME_LENGTH = 255;
    private static final int MAX_HEADER_VALUE_LENGTH = 4096;
    private static final int MAX_RESPONSE_BODY_LENGTH = 1_000_000; // 1 MB of text

    public boolean isMockValid(MockDTO mockDTO) throws MockpitApplicationException{
        if(Objects.isNull(mockDTO)){
            throw new MockpitApplicationException("Mock details not provided.");
        }
        if(Objects.isNull(mockDTO.getName()) || mockDTO.getName().isEmpty()){
            throw new MockpitApplicationException("Mock name not provided.");
        }
        if(mockDTO.getName().length() > MAX_NAME_LENGTH){
            throw new MockpitApplicationException("Mock name is too long.");
        }
        if(Objects.nonNull(mockDTO.getDescription()) && mockDTO.getDescription().length() > MAX_DESCRIPTION_LENGTH){
            throw new MockpitApplicationException("Mock description is too long.");
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
        if(Objects.isNull(responseBodyDTO.getContentType()) || responseBodyDTO.getContentType().isEmpty()){
            throw new MockpitApplicationException("Response body content type not provided.");
        }
        if(Objects.nonNull(responseBodyDTO.getContent())
                && String.valueOf(responseBodyDTO.getContent()).length() > MAX_RESPONSE_BODY_LENGTH){
            throw new MockpitApplicationException("Response body is too large.");
        }
        return true;
    }

    private boolean isResponseHeaderValid(List<ResponseHeaderDTO> responseHeaders) throws MockpitApplicationException {
        if(Objects.nonNull(responseHeaders)){
            for (ResponseHeaderDTO responseHeader : responseHeaders) {
                if(Objects.isNull(responseHeader.getName()) || Objects.isNull(responseHeader.getValue())){
                    throw new MockpitApplicationException("Invalid response headers.");
                }
                if(responseHeader.getName().length() > MAX_HEADER_NAME_LENGTH
                        || responseHeader.getValue().length() > MAX_HEADER_VALUE_LENGTH){
                    throw new MockpitApplicationException("Response header is too long.");
                }
            }
        }
        return true;
    }

    private boolean isRouteValid(RouteDTO routeDTO) throws MockpitApplicationException {
        if(Objects.isNull(routeDTO) || Objects.isNull(routeDTO.getPath())){
            throw new MockpitApplicationException("Route details not provided.");
        }
        if(Objects.isNull(routeDTO.getMethod()) || routeDTO.getMethod().toString().isEmpty()){
            throw new MockpitApplicationException("Route method not provided.");
        }
        if(routeDTO.getPath().isEmpty()){
            throw new MockpitApplicationException("Route path not provided.");
        }
        if(routeDTO.getPath().length() > MAX_PATH_LENGTH){
            throw new MockpitApplicationException("Route path is too long.");
        }
        String normalised = routeDTO.getPath().startsWith("/") ? routeDTO.getPath() : "/" + routeDTO.getPath();
        if(ReservedPathFilter.isReservedPath(normalised)){
            throw new MockpitApplicationException("Route path '" + normalised
                    + "' collides with a reserved internal endpoint.");
        }
        return true;
    }

    private boolean isResponseStatusValid(ResponseStatusDTO responseStatusDTO) throws MockpitApplicationException {
        if(Objects.isNull(responseStatusDTO) || Objects.isNull(responseStatusDTO.getCode()) ){
            throw new MockpitApplicationException("Response status code not provided.");
        }
        return true;
    }
}
