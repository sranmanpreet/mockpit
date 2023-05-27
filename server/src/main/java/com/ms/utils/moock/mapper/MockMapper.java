package com.ms.utils.moock.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.utils.moock.dto.MockDTO;
import com.ms.utils.moock.domain.Mock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring", uses = {ResponseBodyMapper.class,
        ResponseHeaderMapper.class, RouteMapper.class,
        ResponseStatusMapper.class})
public abstract class MockMapper {
    @Mappings({
            @Mapping(source = "responseBody", target = "responseBody"),
            @Mapping(source = "responseHeaders", target = "responseHeaders"),
            @Mapping(source = "responseStatus", target = "responseStatus")
    })
    public abstract MockDTO toDto(Mock mock);

    @Mappings({
            @Mapping(source = "responseBody", target = "responseBody"),
            @Mapping(source = "responseHeaders", target = "responseHeaders"),
            @Mapping(source = "responseStatus", target = "responseStatus")
    })
    public abstract Mock toEntity(MockDTO mockDTO);

    public abstract List<MockDTO> toDTOList(List<Mock> mockList);
    public abstract List<Mock> toEntityList(List<MockDTO> mockDTOList);

    private String responseBodyContentObjectToResponseBodyContentString(Object content){
        if(Objects.isNull(content)){
            return "";
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(content);
        } catch (JsonProcessingException e) {
            // Intentionally blank
        }
        return "";
    }

    private Object responseBodyContentStringToResponseBodyContentObject(String content){
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(content, Object.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
