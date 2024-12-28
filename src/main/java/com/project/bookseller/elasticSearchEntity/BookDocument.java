package com.project.bookseller.elasticSearchEntity;

import com.project.bookseller.dto.book.AuthorDTO;
import com.project.bookseller.dto.book.BookDTO;
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
public class BookDocument {
    @Id
    private String id;
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
    private List<AuthorDTO> authors = new ArrayList<>();

    public static BookDocument convertFromDTO(BookDTO bookDTO) {
        BookDocument bookDocument = new BookDocument();
        bookDocument.setId(String.valueOf(bookDTO.getBookId()));
        bookDocument.setTitle(bookDTO.getTitle());
        bookDocument.setPrice(bookDTO.getPrice());
        bookDocument.setBookDesc(bookDTO.getBookDesc());
        bookDocument.setPages(bookDTO.getPages());
        bookDocument.setPublisher(bookDTO.getPublisher());
        bookDocument.setAuthors(bookDTO.getAuthors());
        return bookDocument;
    }
}
