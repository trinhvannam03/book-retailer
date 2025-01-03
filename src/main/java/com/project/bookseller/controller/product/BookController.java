package com.project.bookseller.controller.product;

import com.project.bookseller.dto.StockRecordDTO;
import com.project.bookseller.dto.book.BookDTO;
import com.project.bookseller.dto.book.CategoryDTO;
import com.project.bookseller.elasticSearchEntity.AuthorDocument;
import com.project.bookseller.elasticSearchEntity.BookDocumentRepository;
import com.project.bookseller.elasticSearchEntity.CategoryDocument;
import com.project.bookseller.entity.book.Book;
import com.project.bookseller.entity.book.BookDocument;
import com.project.bookseller.entity.location.LocationType;
import com.project.bookseller.exceptions.ResourceNotFoundException;
import com.project.bookseller.repository.book.BookRepository;
import com.project.bookseller.service.BookService;
import com.project.bookseller.service.elasticSearch.BookDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/books/")
@RequiredArgsConstructor
public class BookController {
    private final BookRepository bookRepository;
    private final BookService bookService;
    private final BookDocumentService bookDocumentService;
    private final BookDocumentRepository bookDocumentRepository;
    private final ElasticsearchOperations elasticSearchOperations;

    @GetMapping("/{stockRecordId}")
    public ResponseEntity<StockRecordDTO> getBook(@PathVariable Long stockRecordId) {
        try {
            System.out.println("stockRecordId: 2 " + stockRecordId);
            StockRecordDTO stockRecordDTO = bookService.findCompleteBook(stockRecordId);
            return new ResponseEntity<>(stockRecordDTO, HttpStatusCode.valueOf(200));
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatusCode.valueOf(404));
        }
    }

    @GetMapping("/index")
    public ResponseEntity<com.project.bookseller.entity.book.BookDocument> indexAllBooks() {
        List<Book> books = bookRepository.findAllBooks();
        for (Book book : books) {
            com.project.bookseller.entity.book.BookDocument bookDocument = new com.project.bookseller.entity.book.BookDocument();
            bookDocument.setId(String.valueOf(book.getBookId()));
            bookDocument.setTitle(book.getTitle());
            bookDocument.setBook_desc(book.getBookDesc());
            bookDocument.setPages(book.getPages());
            bookDocument.setPrice(book.getPrice());
            bookDocument.setPublisher(book.getPublisher());
            bookDocument.setBook_cover(book.getBookCover());
            bookDocument.setIsbn(book.getIsbn());
            bookDocument.setAuthors(book.getAuthors().stream().map(AuthorDocument::convertFromEntity).toList());
            bookDocument.setCategories(book.getCategories().stream().map(CategoryDocument::convertFromEntity).toList());
            bookDocumentRepository.save(bookDocument);
        }
        return ResponseEntity.ok(null);
    }

    @GetMapping("search")
    public ResponseEntity<List<StockRecordDTO>> searchByKeyword(@RequestParam(required = false) Long location, @RequestParam String keyword, @RequestParam(required = false) Integer page, @RequestParam(required = false) String sort_by, @RequestParam(required = false) List<String> categories, @RequestParam(required = false) Double price_gte, @RequestParam(required = false) String result_type, @RequestParam(required = false) Double price_lte) {

        int currentPage = (page == null || page <= 0) ? 0 : page - 1;
        try {
            List<StockRecordDTO> books = bookDocumentService.searchByKeyword(location, keyword, currentPage, sort_by, categories, price_gte, price_lte);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatusCode.valueOf(500));
        }
    }

    @GetMapping("similar-author")
    public ResponseEntity<List<StockRecordDTO>> getSimilarBooksByAuthor(@RequestParam String author, @RequestParam(required = false) Long location) {
        try {
            List<StockRecordDTO> books = bookDocumentService.getSimilarBooksByAuthor(location, author);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatusCode.valueOf(500));
        }
    }

    @GetMapping("similar")
    public ResponseEntity<List<StockRecordDTO>> getSimilarBooks(@RequestParam String id, @RequestParam(required = false) Long location) throws IOException {
        BookDocument bookDocument = bookDocumentService.getBookDocument(id);
        return ResponseEntity.ok(bookDocumentService.getMoreLikeThis(location, bookDocument));
    }

    @GetMapping("categories")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        try {
            List<CategoryDTO> categories = bookService.findAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatusCode.valueOf(500));
        }
    }

    @GetMapping("/{id}/stores")
    public ResponseEntity<List<StockRecordDTO>> findAvailableStores(@PathVariable Long id) {
        try {
            List<StockRecordDTO> response = bookService.findAvailableStores(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatusCode.valueOf(500));
        }
    }
}
