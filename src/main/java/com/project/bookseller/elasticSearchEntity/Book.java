package com.project.bookseller.elasticSearchEntity;

import com.project.bookseller.entity.book.Author;
import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.ArrayList;
import java.util.List;

@Document(indexName = "books")
@Data
public class Book {
    @Id
    private String bookId;
    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Double)
    private Double price;

    @Field(type = FieldType.Text)
    private String bookDesc;

    @Field(type = FieldType.Integer)
    private Integer pages;

    @Field(type = FieldType.Text)
    private String publisher;

    @Field(type = FieldType.Nested)
    private List<Author> authors = new ArrayList<>();
}
