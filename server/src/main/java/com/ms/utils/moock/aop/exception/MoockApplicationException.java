package com.ms.utils.moock.aop.exception;

public class MoockApplicationException extends Exception {
    String message;

    public MoockApplicationException() {
        super();
    }

    public MoockApplicationException(String message){
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
