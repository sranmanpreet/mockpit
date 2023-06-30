package com.ms.utils.moock.mapper;

import com.ms.utils.moock.domain.*;
import com.ms.utils.moock.dto.MockDTO;
import org.mapstruct.*;

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
    public abstract MockDTO toDto(Mock mock);

    @Mappings({
            @Mapping(source = "responseBody", target = "responseBody"),
            @Mapping(source = "responseHeaders", target = "responseHeaders"),
            @Mapping(source = "responseStatus", target = "responseStatus")
    })
    public abstract Mock toEntity(MockDTO mockDTO);

    public abstract List<MockDTO> toDTOList(List<Mock> mockList);
    public abstract List<Mock> toEntityList(List<MockDTO> mockDTOList);

}
