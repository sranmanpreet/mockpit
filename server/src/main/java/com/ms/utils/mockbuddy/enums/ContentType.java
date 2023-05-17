package com.ms.utils.mockbuddy.enums;

public enum ContentType {
    APPLICATION_JSON("application/json"),
    APPLICATION_TEXT("application/text");

    public final String value;

    private ContentType(String value){
        this.value = value;
    }
}
