package com.ms.utils.moock.mapper;

import com.ms.utils.moock.domain.Mock;
import com.ms.utils.moock.dto.ExportMockDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ResponseBodyMapper.class,
        ResponseHeaderMapper.class, RouteMapper.class,
        ResponseStatusMapper.class})
public abstract class ExportMockMapper {
    @Mappings({
            @Mapping(source = "responseBody", target = "responseBody"),
            @Mapping(source = "responseHeaders", target = "responseHeaders"),
            @Mapping(source = "responseStatus", target = "responseStatus")
    })
    public abstract ExportMockDTO toDto(Mock mock);

    @Mappings({
            @Mapping(source = "responseBody", target = "responseBody"),
            @Mapping(source = "responseHeaders", target = "responseHeaders"),
            @Mapping(source = "responseStatus", target = "responseStatus")
    })
    public abstract Mock toEntity(ExportMockDTO mockDTO);

    public abstract List<ExportMockDTO> toDTOList(List<Mock> mockList);
    public abstract List<Mock> toEntityList(List<ExportMockDTO> mockDTOList);

}
