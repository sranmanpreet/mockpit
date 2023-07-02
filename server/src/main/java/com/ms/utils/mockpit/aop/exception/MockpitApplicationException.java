package com.ms.utils.mockpit.aop.exception;

public class MockpitApplicationException extends Exception {
    String message;

    public MockpitApplicationException() {
        super();
    }

    public MockpitApplicationException(String message){
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
