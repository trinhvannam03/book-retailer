package com.project.bookseller.exceptions;

import lombok.Data;

@Data
public class BadCredentialsException extends RuntimeException {
    public static String INVALID_CREDENTIALS = "Invalid credentials";
    private String message;

    public BadCredentialsException(String message) {
        super(message);
    }
}
