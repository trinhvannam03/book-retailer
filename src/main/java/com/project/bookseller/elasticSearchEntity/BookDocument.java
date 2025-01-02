package com.project.bookseller.elasticSearchEntity;

import com.project.bookseller.dto.book.AuthorDTO;
import com.project.bookseller.dto.book.BookDTO;
import com.project.bookseller.entity.book.Book;
import com.project.bookseller.entity.book.Category;
import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(indexName = "books")
@Data
public class BookDocument implements Serializable {
    @Serial
    private static final long serialVersionUID = 10L;
    @Id
    private String id;
    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Double)
    private Double price;

    @Field(type = FieldType.Text)
    private String book_desc;


    @Field(type = FieldType.Text)
    private String book_cover;

    @Field(type = FieldType.Integer)
    private Integer pages;

    @Field(type = FieldType.Text)
    private String publisher;
    @Field(type = FieldType.Text)
    private String isbn;

    @Field(type = FieldType.Nested)
    private List<AuthorDocument> authors = new ArrayList<>();

    @Field(type = FieldType.Nested)
    private List<CategoryDocument> categories = new ArrayList<>();
//
//    @Field(type = FieldType.Date, format = DateFormat.date_time)
//    private Date publication_date;

    public static BookDocument convertFromDTO(BookDTO bookDTO) {
        BookDocument bookDocument = new BookDocument();
        bookDocument.setId(String.valueOf(bookDTO.getBookId()));
        bookDocument.setTitle(bookDTO.getTitle());
        bookDocument.setPrice(bookDTO.getPrice());
        bookDocument.setBook_desc(bookDTO.getBookDesc());
        bookDocument.setPages(bookDTO.getPages());
        bookDocument.setPublisher(bookDTO.getPublisher());
        bookDocument.setIsbn(bookDTO.getIsbn());
//        bookDocument.setPublication_date(bookDTO.getPublicationDate());
        return bookDocument;
    }

}
