package com.project.bookseller.entity.book;

import com.project.bookseller.dto.book.BookDTO;
import com.project.bookseller.elasticSearchEntity.AuthorDocument;
import com.project.bookseller.elasticSearchEntity.CategoryDocument;
import com.project.bookseller.interfaces.Book;
import jakarta.persistence.Id;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(indexName = "book_documents")
public class BookDocument implements Book {
    @Id
    private String id;
    @Field(type = FieldType.Text)
    private String title;
    @Field(type = FieldType.Text)
    private String publisher;
    @Field(type = FieldType.Text)
    private String isbn;
    @Field(type = FieldType.Text)
    private String book_desc;
    @Field(type = FieldType.Text)
    private String book_cover;
    @Field(type = FieldType.Long)
    private Long bookId;
    @Field(type = FieldType.Double)
    private Double price;
    @Field(type = FieldType.Integer)
    private Integer pages;
    @Field(type = FieldType.Nested)
    private List<CategoryDocument> categories = new ArrayList<>();
    @Field(type = FieldType.Nested)
    private List<AuthorDocument> authors = new ArrayList<>();

    public static BookDocument convertFromDTO(BookDTO bookDTO) {
        BookDocument bookDocument = new BookDocument();
        return bookDocument;
    }
}
