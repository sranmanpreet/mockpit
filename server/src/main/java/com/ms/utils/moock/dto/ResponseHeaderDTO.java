package com.ms.utils.moock.dto;

import com.ms.utils.moock.domain.Mock;

import java.time.LocalDateTime;

public class ResponseHeaderDTO {
    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
