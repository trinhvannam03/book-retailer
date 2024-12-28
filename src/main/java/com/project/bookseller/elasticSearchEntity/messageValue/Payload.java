package com.project.bookseller.elasticSearchEntity.messageValue;

import com.project.bookseller.elasticSearchEntity.Book;
import lombok.Data;

import java.util.List;

@Data
public class Payload {
    private Book before;
    private Book after;
}
