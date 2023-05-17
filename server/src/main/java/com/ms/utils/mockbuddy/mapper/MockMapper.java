package com.ms.utils.mockbuddy.mapper;

import com.ms.utils.mockbuddy.dto.MockDTO;
import com.ms.utils.mockbuddy.domain.Mock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ResponseBodyMapper.class,
        ResponseHeaderMapper.class, RouteMapper.class,
        ResponseStatusMapper.class})
public interface MockMapper {
    @Mappings({
            @Mapping(source = "responseBody", target = "responseBody"),
            @Mapping(source = "responseHeaders", target = "responseHeaders"),
            @Mapping(source = "responseStatus", target = "responseStatus")
    })
    MockDTO toDto(Mock mock);

    @Mappings({
            @Mapping(source = "responseBody", target = "responseBody"),
            @Mapping(source = "responseHeaders", target = "responseHeaders"),
            @Mapping(source = "responseStatus", target = "responseStatus")
    })
    Mock toEntity(MockDTO mockDTO);

    List<MockDTO> toDTOList(List<Mock> mockList);
    List<Mock> toEntityList(List<MockDTO> mockDTOList);
}
