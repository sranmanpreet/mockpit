package com.ms.utils.mockpit.mapper;

import com.ms.utils.mockpit.dto.ResponseHeaderDTO;
import com.ms.utils.mockpit.domain.ResponseHeader;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface ResponseHeaderMapper {
    ResponseHeaderDTO toDto(ResponseHeader responseHeader);
    ResponseHeader toEntity(ResponseHeaderDTO responseHeaderDTO);

    List<ResponseHeaderDTO> toDTOList(List<ResponseHeader> headerList);
    List<ResponseHeader> toEntityList(List<ResponseHeaderDTO> headerDTOList);
}
