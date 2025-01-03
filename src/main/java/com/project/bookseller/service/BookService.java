package com.project.bookseller.service;


import com.project.bookseller.dto.LocationDTO;
import com.project.bookseller.dto.StockRecordDTO;
import com.project.bookseller.dto.book.AuthorDTO;
import com.project.bookseller.dto.book.BookDTO;
import com.project.bookseller.dto.book.CategoryDTO;
import com.project.bookseller.entity.book.Author;
import com.project.bookseller.entity.book.Book;
import com.project.bookseller.entity.book.Category;
import com.project.bookseller.entity.location.LocationType;
import com.project.bookseller.entity.location.StockRecord;
import com.project.bookseller.exceptions.ResourceNotFoundException;
import com.project.bookseller.repository.StockRecordRepository;
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
    private final StockRecordRepository stockRecordRepository;

    public StockRecordDTO findCompleteBook(Long stockRecordId) throws ResourceNotFoundException {
        Optional<StockRecord> optional = stockRecordRepository.findStockRecordByStockRecordId(stockRecordId);
        System.out.println("stockRecordId: " + stockRecordId);
        if (optional.isPresent()) {
            StockRecord stockRecord = optional.get();
            Book book = stockRecord.getBook();
            StockRecordDTO stockRecordDTO = StockRecordDTO.convertFromEntity(stockRecord);
            BookDTO bookDTO = BookDTO.convertFromEntity(book);
            List<CategoryDTO> categories = new ArrayList<>();
            List<AuthorDTO> authors = new ArrayList<>();
            for (Category category : book.getCategories()) {
                CategoryDTO categoryDTO = CategoryDTO.convertFromCategory(category);
                categories.add(categoryDTO);
            }
            for (Author author : book.getAuthors()) {
                AuthorDTO authorDTO = AuthorDTO.convertFromEntity(author);
                authors.add(authorDTO);
            }
            bookDTO.setCategories(categories);
            bookDTO.setAuthors(authors);
            stockRecordDTO.setBook(bookDTO);
            return stockRecordDTO;
        }
        throw new ResourceNotFoundException("Book not found!");
    }

    public List<CategoryDTO> findAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryDTO> categoriesDTO = new ArrayList<>();
        for (Category category : categories) {
            CategoryDTO categoryDTO = CategoryDTO.convertFromCategory(category);
            categoriesDTO.add(categoryDTO);
        }
        return categoriesDTO;
    }

    public List<StockRecordDTO> findAvailableStores(Long bookId) {
        List<StockRecord> stockRecords = stockRecordRepository.findStockRecordsByBookIdAndLocationType(bookId, LocationType.STORE);
        return stockRecords.stream().map(r -> {
            StockRecordDTO stockRecordDTO = StockRecordDTO.convertFromEntity(r);
            stockRecordDTO.setLocation(LocationDTO.convertFromEntity(r.getLocation()));
            return stockRecordDTO;
        }).toList();
    }
}
