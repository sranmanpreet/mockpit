package com.ms.utils.moock.dto;

import com.ms.utils.moock.domain.Mock;

import java.time.LocalDateTime;

public class ResponseStatusDTO {
    private Long id;
    private int code;
    private Mock mock;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Mock getMock() {
        return mock;
    }

    public void setMock(Mock mock) {
        this.mock = mock;
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
