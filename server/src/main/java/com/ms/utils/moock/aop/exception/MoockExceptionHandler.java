package com.ms.utils.moock.aop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class MoockExceptionHandler {

    @ExceptionHandler(MockNotFoundException.class)
    protected ResponseEntity<Object> handleMockNotFoundException(MockNotFoundException e){
        return new ResponseEntity<Object>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MoockApplicationException.class)
    protected ResponseEntity<Object> handleMoockApplicationException(MoockApplicationException e){
        return new ResponseEntity<Object>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleException(Exception e){
        return new ResponseEntity<Object>(e.getMessage(), HttpStatus.BAD_GATEWAY);
    }
}
