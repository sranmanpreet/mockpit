package com.ms.utils.moock.mapper;

import com.ms.utils.moock.domain.ResponseBody;
import com.ms.utils.moock.dto.ResponseBodyDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ResponseBodyMapper {

    ResponseBodyDTO toDto(ResponseBody responseBody);

    ResponseBody toEntity(ResponseBodyDTO responseBodyDTO);

    List<ResponseBodyDTO> toDTOList(List<ResponseBody> responseBodyList);
    List<ResponseBody> toEntityList(List<ResponseBodyDTO> responseBodyDTOList);
}
