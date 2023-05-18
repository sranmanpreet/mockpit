package com.ms.utils.moock.mapper;

import com.ms.utils.moock.domain.ResponseStatus;
import com.ms.utils.moock.dto.ResponseStatusDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ResponseStatusMapper {

    ResponseStatusDTO toDto(ResponseStatus responseStatus);
    ResponseStatus toEntity(ResponseStatusDTO responseStatusDTO);

    List<ResponseStatusDTO> toDTOList(List<ResponseStatus> statusList);
    List<ResponseStatus> toEntityList(List<ResponseStatusDTO> statusDTOList);
}
