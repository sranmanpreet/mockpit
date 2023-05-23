package com.ms.utils.moock.dto;

import java.util.List;

public class LiveResponseDTO {
    String body;
    List<ResponseHeaderDTO> headers;
    int statusCode;

    public LiveResponseDTO(){

    }

    public LiveResponseDTO(String body, int statusCode, List<ResponseHeaderDTO> headers) {
        this.body = body;
        this.statusCode = statusCode;
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public List<ResponseHeaderDTO> getHeaders() {
        return headers;
    }

    public void setHeaders(List<ResponseHeaderDTO> headers) {
        this.headers = headers;
    }
}
