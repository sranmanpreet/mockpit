package com.ms.utils.moock.dto;

import java.util.List;

public class LiveResponseDTO {
    ResponseBodyDTO body;
    List<ResponseHeaderDTO> headers;

    public LiveResponseDTO(){

    }

    public LiveResponseDTO(ResponseBodyDTO body, List<ResponseHeaderDTO> headers) {
        this.body = body;
        this.headers = headers;
    }

    public ResponseBodyDTO getBody() {
        return body;
    }

    public void setBody(ResponseBodyDTO body) {
        this.body = body;
    }

    public List<ResponseHeaderDTO> getHeaders() {
        return headers;
    }

    public void setHeaders(List<ResponseHeaderDTO> headers) {
        this.headers = headers;
    }
}
