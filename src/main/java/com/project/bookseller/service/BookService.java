package com.project.bookseller.service;


import com.project.bookseller.dto.book.AuthorDTO;
import com.project.bookseller.dto.book.BookDTO;
import com.project.bookseller.dto.book.CategoryDTO;
import com.project.bookseller.entity.book.Author;
import com.project.bookseller.entity.book.Book;
import com.project.bookseller.entity.book.Category;
import com.project.bookseller.entity.location.LocationType;
import com.project.bookseller.exceptions.ResourceNotFoundException;
import com.project.bookseller.repository.book.BookRepository;
import com.project.bookseller.repository.book.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;


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
            try {
                bookDTO.setStock(book.getStockRecords().get(0).getQuantity());
                return bookDTO;
            } catch (Exception e) {
                e.printStackTrace();
                throw new ResourceNotFoundException("Book not found!");
            }
        }
        throw new ResourceNotFoundException("Book not found!");
    }
}
