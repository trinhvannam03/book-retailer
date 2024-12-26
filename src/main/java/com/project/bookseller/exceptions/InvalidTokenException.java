package com.project.bookseller.exceptions;

import lombok.Data;

public class InvalidTokenException  extends Exception{
    public InvalidTokenException(String message){
        super(message);
    }
}
