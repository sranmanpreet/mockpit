package com.ms.utils.mockpit.dto;

import org.springframework.http.HttpMethod;

public class RouteDTO {
    private String path;
    private HttpMethod method;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }
}
