package com.ms.utils.mockpit.mapper;

import com.ms.utils.mockpit.domain.Mock;
import com.ms.utils.mockpit.dto.MockDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Maps between {@link Mock} and {@link MockDTO}. Auth-related fields are deliberately NOT mapped
 * here because {@code AuthConfig} requires the {@code AuthConfigCodec} (encryption / hashing /
 * redaction) which has side effects unsuitable for a pure mapper. {@code MockService} populates
 * those fields explicitly after invoking the mapper.
 */
@Mapper(componentModel = "spring",
        uses = {ResponseBodyMapper.class, ResponseHeaderMapper.class, RouteMapper.class, ResponseStatusMapper.class})
public interface MockMapper {

    @Mapping(target = "authConfig", ignore = true)
    @Mapping(target = "authConfigRaw", ignore = true)
    @Mapping(target = "authFailure", ignore = true)
    MockDTO toDto(Mock mock);

    @Mapping(target = "authType", ignore = true)
    @Mapping(target = "authConfigJson", ignore = true)
    @Mapping(target = "authFailureStatus", ignore = true)
    @Mapping(target = "authFailureBody", ignore = true)
    @Mapping(target = "authFailureContentType", ignore = true)
    @Mapping(target = "userId", ignore = true)
    Mock toEntity(MockDTO mockDTO);

    List<MockDTO> toDTOList(List<Mock> mockList);
    List<Mock> toEntityList(List<MockDTO> mockDTOList);
}
