package com.ms.utils.mockpit.mapper;

import com.ms.utils.mockpit.domain.ResponseStatus;
import com.ms.utils.mockpit.dto.ResponseStatusDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ResponseStatusMapper {

    ResponseStatusDTO toDto(ResponseStatus responseStatus);
    ResponseStatus toEntity(ResponseStatusDTO responseStatusDTO);

    List<ResponseStatusDTO> toDTOList(List<ResponseStatus> statusList);
    List<ResponseStatus> toEntityList(List<ResponseStatusDTO> statusDTOList);
}
