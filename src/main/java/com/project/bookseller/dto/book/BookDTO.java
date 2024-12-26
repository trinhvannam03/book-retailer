package com.project.bookseller.dto.book;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.bookseller.dto.StockRecordDTO;
import com.project.bookseller.entity.book.Book;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BookDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 4159557539810053990L;
    private long id;
    private String title;
    private String bookDesc;
    private String coverType;
    private String isbn;
    private String bookCover;
    private Integer bookWidth;
    private Integer bookHeight;
    private Integer bookLength;
    private Integer bookWeight;
    private Integer pages;
    private Double price;
    private String publisher;
    private Integer stock;
    private List<AuthorDTO> authors = new ArrayList<>();
    private List<CategoryDTO> categories = new ArrayList<>();
    private List<StockRecordDTO> stockRecords = new ArrayList<>();

    public static BookDTO convertFromBook(Book book) {
        BookDTO bookDTO = new BookDTO();
        bookDTO.setId(book.getBookId());
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
        return bookDTO;
    }
}
