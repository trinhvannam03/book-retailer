package com.project.bookseller.service;


import com.project.bookseller.dto.*;
import com.project.bookseller.entity.book.Author;
import com.project.bookseller.entity.book.Book;
import com.project.bookseller.entity.book.Category;
import com.project.bookseller.entity.location.StockRecord;
import com.project.bookseller.entity.location.LocationType;
import com.project.bookseller.exceptions.ResourceNotFoundException;
import com.project.bookseller.repository.book.BookRepository;
import com.project.bookseller.repository.book.CategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final Connection connection;

    @PersistenceContext
    private EntityManager entityManager;

    public List<BookDTO> getBriefBooksByBookIds(List<Long> bookIds) {
        List<Book> books = bookRepository.findBriefBooksByBookIds(bookIds);
        List<BookDTO> bookDTOs = new ArrayList<>();
        for (Book book : books) {
            BookDTO bookDTO = new BookDTO();
            int stock = book.getStockRecords().get(0).getQuantity();
            bookDTO.setStock(stock);
            bookDTOs.add(bookDTO);
        }
        return bookDTOs;
    }

    public BookDTO findBriefBook(String isbn) throws ResourceNotFoundException {
        String sql = "select b.*, a.stock from book b left join (select book_id, COALESCE(SUM(quantity), 0)" +
                " as stock from bookchain.stock_record group by book_id) " +
                "a on b.book_id = a.book_id where b.isbn = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, isbn);
            ResultSet rs = statement.executeQuery();
            BookDTO bookDTO = new BookDTO();

            while (rs.next()) {
                int bookId = rs.getInt("book_id");
                String isbnValue = rs.getString("isbn");
                String title = rs.getString("title");
                int stock = rs.getInt("stock");
                String coverType = rs.getString("cover_type");
                String bookCover = rs.getString("book_cover");
                bookDTO.setId(bookId);
                bookDTO.setIsbn(isbnValue);
                bookDTO.setTitle(title);
                bookDTO.setStock(stock);
                bookDTO.setCoverType(coverType);
                bookDTO.setBookCover(bookCover);

            }
            return bookDTO;
        } catch (SQLException e) {
            throw new ResourceNotFoundException("No book found!");
        }
    }

    @Data
    public static class UpdateCategory {
        List<Long> categoryIds = new ArrayList<>();
        private Book book;
    }

    @Cacheable(value = "books", key = "#isbn")
    public BookDTO getBook(String isbn) {
        BookDTO bookDTO = new BookDTO();
        Optional<Book> optionalBook = bookRepository.findBookByIsbnWithCategories(isbn);
        if (optionalBook.isPresent()) {
            Book book = optionalBook.get();
            bookDTO = BookDTO.convertFromBook(book);
            return bookDTO;
        }
        return null;
    }


    public BookDTO findCompleteBook(String isbn) throws ResourceNotFoundException {
        Optional<Book> optionalBook = bookRepository.findBookByIsbnWithStockRecordsAndLocationType(isbn, LocationType.ONLINE_STORE);
        if (optionalBook.isPresent()) {
            Book book = optionalBook.get();
            BookDTO bookDTO = BookDTO.convertFromBook(optionalBook.get());
            List<CategoryDTO> categories = new ArrayList<>();
            for (Category category : book.getCategories()) {
                CategoryDTO categoryDTO = CategoryDTO.convertFromCategory(category);
                categories.add(categoryDTO);
            }
            bookDTO.setCategories(categories);
            List<AuthorDTO> authors = new ArrayList<>();
            for (Author author : book.getAuthors()) {
                AuthorDTO authorDTO = AuthorDTO.convertFromEAuthor(author);
                authors.add(authorDTO);
            }
            bookDTO.setAuthors(authors);
            List<StockRecordDTO> stockRecords = new ArrayList<>();
            int quantity = 0;
            for (StockRecord stockRecord : book.getStockRecords()) {
                quantity += stockRecord.getQuantity();
            }
            bookDTO.setStock(quantity);
            return bookDTO;
        }
        throw new ResourceNotFoundException("Book not found!");
    }

    //REFINED
}
