package com.ms.utils.moock.mapper;

import com.ms.utils.moock.dto.LiveResponseDTO;
import com.ms.utils.moock.dto.MockDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {MockDTO.class,
        LiveResponseDTO.class})
public abstract class MockDTOLiveResponseDTOMapper {

    public LiveResponseDTO toLiveResponseDTO(MockDTO mockDto) {
        LiveResponseDTO liveResponseDTO = new LiveResponseDTO();
        liveResponseDTO.setHeaders(mockDto.getResponseHeaders());
        liveResponseDTO.setStatusCode(mockDto.getResponseStatus().getCode());
        liveResponseDTO.setContentType(mockDto.getResponseBody().getContentType());
        liveResponseDTO.setBody(mockDto.getResponseBody().getContent());
        return liveResponseDTO;
    }

}
