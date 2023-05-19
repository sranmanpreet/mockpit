package com.ms.utils.moock.mapper;

import com.ms.utils.moock.domain.Mock;
import com.ms.utils.moock.dto.LiveResponseDTO;
import com.ms.utils.moock.dto.MockDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", uses = {MockDTO.class,
        LiveResponseDTO.class})
public interface MockDTOLiveResponseDTOMapper {

    @Mappings({
            @Mapping(source = "responseBody", target = "body"),
            @Mapping(source = "responseHeaders", target = "headers")
    })
    LiveResponseDTO toLiveResponseDTO(MockDTO mockDto);

}