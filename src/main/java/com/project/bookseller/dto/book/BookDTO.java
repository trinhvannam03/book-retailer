package com.project.bookseller.dto.book;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.bookseller.dto.StockRecordDTO;
import com.project.bookseller.entity.book.Book;
import com.project.bookseller.enums.BookLanguage;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BookDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 4159557539810053990L;
    private String title;
    private String bookDesc;
    private String coverType;
    private String isbn;
    private String bookCover;
    private Integer bookWidth;
    private Integer bookHeight;
    private Integer bookLength;
    private Integer bookWeight;
    private Date publicationDate;
    private Integer pages;
    private long bookId;
    private Double price;
    private String publisher;
    private BookLanguage bookLanguage;
    private List<AuthorDTO> authors = new ArrayList<>();
    private List<CategoryDTO> categories = new ArrayList<>();

    public static BookDTO convertFromEntity(Book book) {
        BookDTO bookDTO = new BookDTO();
        bookDTO.setBookId(book.getBookId());
        bookDTO.setTitle(book.getTitle());
        bookDTO.setBookDesc(book.getBookDesc());
        bookDTO.setIsbn(book.getIsbn());
        bookDTO.setBookCover(book.getBookCover());
        bookDTO.setBookWidth(book.getBookWidth());
        bookDTO.setBookHeight(book.getBookHeight());
        bookDTO.setBookLength(book.getBookLength());
        bookDTO.setBookWeight(book.getBookWeight());
        bookDTO.setPages(book.getPages());
        bookDTO.setPrice(book.getPrice());
        bookDTO.setPublisher(book.getPublisher());
        bookDTO.setCoverType(book.getCoverType());
        bookDTO.setBookLanguage(book.getBookLanguage());
        return bookDTO;
    }
}