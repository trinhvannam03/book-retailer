package com.project.bookseller.exceptions;

import lombok.Data;

@Data
public class DataMismatchException extends RuntimeException {
    public static final String DATA_MISMATCH = "Data mismatch";
    private String message;
    public DataMismatchException(String message) {
        this.message = message;
    }
}
