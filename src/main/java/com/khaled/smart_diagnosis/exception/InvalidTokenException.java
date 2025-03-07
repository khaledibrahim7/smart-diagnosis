package com.khaled.smart_diagnosis.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidTokenException extends RuntimeException{
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
