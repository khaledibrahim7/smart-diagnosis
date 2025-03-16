package com.khaled.smart_diagnosis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {


    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "This email is already registered.");
    }

    @ExceptionHandler(PasswordsDoNotMatchException.class)
    public ResponseEntity<Map<String, Object>> handlePasswordsDoNotMatch(PasswordsDoNotMatchException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Passwords do not match.");
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationFailed(AuthenticationFailedException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Authentication failed.");
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<String> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "User not found.");
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<Map<String, Object>> handleDatabaseException(DatabaseException ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while processing your request.");
    }

    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<Map<String, Object>> handleEmailSendingException(EmailSendingException ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while sending the welcome email.");
    }

    @ExceptionHandler(TokenGenerationException.class)
    public ResponseEntity<Map<String, Object>> handleTokenGenerationException(TokenGenerationException ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while generating the authentication token.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
    }

    @ExceptionHandler(InvalidPhoneNumberException.class)
    public ResponseEntity<String> handleInvalidPhoneNumberException(InvalidPhoneNumberException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
