package com.ms.utils.moock.dto;

import java.util.List;

public class LiveResponseDTO {
    Object body;
    List<ResponseHeaderDTO> headers;
    String contentType;
    int statusCode;

    public LiveResponseDTO(){

    }

    public LiveResponseDTO(Object body, int statusCode, List<ResponseHeaderDTO> headers) {
        this.body = body;
        this.statusCode = statusCode;
        this.headers = headers;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
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
