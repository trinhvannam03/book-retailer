package com.project.bookseller.exceptions.handler;

import com.project.bookseller.exceptions.PassWordNotMatch;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<Map<String, Object>> handleValidationException(Exception e) {
        Map<String, Object> errorMessages = new HashMap<>();
        MethodArgumentNotValidException validException = (MethodArgumentNotValidException) e;
        validException.getBindingResult().getFieldErrors().forEach(fieldError -> {
            errorMessages.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        return new ResponseEntity<>(errorMessages, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({PassWordNotMatch.class})
    public ResponseEntity<Map<String, Object>> handlePassWordNotMatchException(Exception e) {
        Map<String, Object> errorMessages = new HashMap<>();
        errorMessages.put("message", e.getMessage());
        return new ResponseEntity<>(errorMessages, HttpStatus.BAD_REQUEST);
    }
}
