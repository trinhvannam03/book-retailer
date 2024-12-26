package com.project.bookseller.exceptions;

import lombok.Data;

@Data
public class PassWordNotMatch extends Exception {
    private String fieldName;
    private String message;
    public PassWordNotMatch(String fieldName, String message) {
        this.fieldName = fieldName;
        this.message = message;
    }


}
