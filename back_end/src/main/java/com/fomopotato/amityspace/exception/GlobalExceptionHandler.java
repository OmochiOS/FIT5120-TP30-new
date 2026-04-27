package com.fomopotato.amityspace.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LocationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleLocationNotFound(LocationNotFoundException e) {
        return Map.of(
                "status", 404,
                "error", e.getMessage()
        );
    }

    @ExceptionHandler(SensoryProfileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleSensoryProfileNotFound(SensoryProfileNotFoundException e) {
        return Map.of(
                "status", 404,
                "error", e.getMessage()
        );
    }
}