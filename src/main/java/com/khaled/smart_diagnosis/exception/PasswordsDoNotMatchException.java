package com.khaled.smart_diagnosis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PasswordsDoNotMatchException extends RuntimeException{
    public PasswordsDoNotMatchException(String message) {
        super(message);
    }
}
