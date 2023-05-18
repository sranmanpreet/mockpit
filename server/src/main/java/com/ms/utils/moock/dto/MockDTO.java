package com.ms.utils.moock.dto;

import java.time.LocalDateTime;
import java.util.List;

public class MockDTO {

    private Long id;
    private String name;
    private String description;
    private RouteDTO route;
    private List<ResponseHeaderDTO> responseHeaders;
    private ResponseBodyDTO responseBody;
    private ResponseStatusDTO responseStatus;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RouteDTO getRoute() {
        return route;
    }

    public void setRoute(RouteDTO route) {
        this.route = route;
    }

    public List<ResponseHeaderDTO> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(List<ResponseHeaderDTO> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public ResponseBodyDTO getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(ResponseBodyDTO responseBody) {
        this.responseBody = responseBody;
    }

    public ResponseStatusDTO getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(ResponseStatusDTO responseStatus) {
        this.responseStatus = responseStatus;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }
}
