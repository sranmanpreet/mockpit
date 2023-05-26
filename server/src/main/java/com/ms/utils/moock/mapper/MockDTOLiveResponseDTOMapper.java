package com.ms.utils.moock.mapper;

import com.ms.utils.moock.dto.LiveResponseDTO;
import com.ms.utils.moock.dto.MockDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", uses = {MockDTO.class,
        LiveResponseDTO.class})
public interface MockDTOLiveResponseDTOMapper {

    @Mappings({
            @Mapping(source = "responseBody.content", target = "body"),
            @Mapping(source = "responseBody.contentType", target = "contentType"),
            @Mapping(source = "responseStatus.code", target = "statusCode"),
            @Mapping(source = "responseHeaders", target = "headers")
    })
    LiveResponseDTO toLiveResponseDTO(MockDTO mockDto);

}
