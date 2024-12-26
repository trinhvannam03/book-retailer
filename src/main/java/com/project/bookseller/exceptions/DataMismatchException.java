package com.project.bookseller.exceptions;

import lombok.Data;

@Data
public class DataMismatchException extends RuntimeException {
    private String message;
    public DataMismatchException(String message) {
        this.message = message;
    }
}
