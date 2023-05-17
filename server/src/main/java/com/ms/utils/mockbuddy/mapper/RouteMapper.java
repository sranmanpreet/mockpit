package com.ms.utils.mockbuddy.mapper;

import com.ms.utils.mockbuddy.dto.RouteDTO;
import com.ms.utils.mockbuddy.domain.Route;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RouteMapper {
    RouteDTO toDto(Route route);
    Route toEntity(RouteDTO routeDTO);

    List<RouteDTO> toDTOList(List<Route> routeList);
    List<Route> toEntityList(List<RouteDTO> routeDTOList);
}
