package com.project.bookseller.exceptions;

import lombok.Data;

@Data
public class UserNotFoundException extends Exception {
    private String errorName;
    private String message;

    public UserNotFoundException(String errorName, String message) {
        this.errorName = errorName;
        this.message = message;
    }
}
