package com.project.bookseller.exceptions;

import lombok.Data;

@Data
public class InvalidCredentialsException extends RuntimeException {
    public static String INVALID_CREDENTIALS = "Invalid credentials";
    private String message;

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
