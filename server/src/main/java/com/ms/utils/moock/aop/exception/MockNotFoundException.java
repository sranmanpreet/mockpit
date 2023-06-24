package com.ms.utils.moock.aop.exception;


public class MockNotFoundException extends Exception {

    String message;

    public MockNotFoundException() {
        super();
    }

    public MockNotFoundException(String message){
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
