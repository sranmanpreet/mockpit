package com.ms.utils.mockbuddy.mapper;

import com.ms.utils.mockbuddy.dto.ResponseHeaderDTO;
import com.ms.utils.mockbuddy.domain.ResponseHeader;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ResponseHeaderMapper {
    ResponseHeaderDTO toDto(ResponseHeader responseHeader);
    ResponseHeader toEntity(ResponseHeaderDTO responseHeaderDTO);

    List<ResponseHeaderDTO> toDTOList(List<ResponseHeader> headerList);
    List<ResponseHeader> toEntityList(List<ResponseHeaderDTO> headerDTOList);
}
