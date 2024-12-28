package com.project.bookseller.elasticSearchEntity.messageValue;

import lombok.Data;

import java.util.List;

@Data
public class Schema {
    private String type;
    private List<Field> fields;
    private boolean optional;
    private String name;
}
