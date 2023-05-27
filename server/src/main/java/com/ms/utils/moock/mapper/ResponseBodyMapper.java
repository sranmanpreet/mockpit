package com.ms.utils.moock.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.utils.moock.domain.ResponseBody;
import com.ms.utils.moock.dto.ResponseBodyDTO;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring")
public abstract class ResponseBodyMapper {

    public ResponseBodyDTO toDto(ResponseBody responseBody){
        ResponseBodyDTO responseBodyDTO = new ResponseBodyDTO();
        responseBodyDTO.setType(responseBody.getType());
        responseBodyDTO.setContentType(responseBody.getContentType());
        responseBodyDTO.setContent(contentStringToContentObject(responseBody.getContent()));
        return responseBodyDTO;
    }

    public ResponseBody toEntity(ResponseBodyDTO responseBodyDTO) {
        ResponseBody responseBody = new ResponseBody();
        responseBody.setType(responseBodyDTO.getType());
        responseBody.setContentType(responseBodyDTO.getContentType());
        responseBody.setContent(contentObjectToContentString(responseBodyDTO.getContent()));
        return responseBody;
    }

    abstract List<ResponseBodyDTO> toDTOList(List<ResponseBody> responseBodyList);
    abstract List<ResponseBody> toEntityList(List<ResponseBodyDTO> responseBodyDTOList);

    private String contentObjectToContentString(Object content){
        if(Objects.isNull(content)){
            return "";
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(content);
        } catch (JsonProcessingException e) {
            // Intentionally blank
        }
        return "";
    }

    private Object contentStringToContentObject(String content){
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(content, Object.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
