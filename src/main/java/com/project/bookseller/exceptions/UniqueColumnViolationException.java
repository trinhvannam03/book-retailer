package com.project.bookseller.exceptions;

import lombok.Data;

@Data
public class UniqueColumnViolationException extends RuntimeException {
    public static final String EMAIL_ALREADY_EXISTS = "Email already exists";
    private String message;
    public UniqueColumnViolationException(String message) {
        this.message = message;
    }
}
