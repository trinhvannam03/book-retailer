package com.project.bookseller.elasticSearchEntity.messageValue;

import lombok.Data;

@Data
public class Field {
    private String type;
    private boolean optional;
    private String field;
}
