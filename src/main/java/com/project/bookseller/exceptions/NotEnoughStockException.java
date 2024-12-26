package com.project.bookseller.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class NotEnoughStockException extends Exception {
    private String message;

    public NotEnoughStockException(String message) {
        super(message);
    }
}
