package com.project.bookseller.exceptions;


public class ResourceNotFoundException extends RuntimeException {
    public static final String NO_SUCH_ITEM = "No such item";
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
