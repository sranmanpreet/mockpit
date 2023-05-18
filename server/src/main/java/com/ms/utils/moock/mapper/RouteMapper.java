package com.ms.utils.moock.mapper;

import com.ms.utils.moock.dto.RouteDTO;
import com.ms.utils.moock.domain.Route;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RouteMapper {
    RouteDTO toDto(Route route);
    Route toEntity(RouteDTO routeDTO);

    List<RouteDTO> toDTOList(List<Route> routeList);
    List<Route> toEntityList(List<RouteDTO> routeDTOList);
}
