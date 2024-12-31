package com.project.bookseller.controller.product;

import com.project.bookseller.dto.book.AuthorDTO;
import com.project.bookseller.dto.book.BookDTO;
import com.project.bookseller.elasticSearchEntity.AuthorDocument;
import com.project.bookseller.elasticSearchEntity.BookDocument;
import com.project.bookseller.elasticSearchEntity.CategoryDocument;
import com.project.bookseller.entity.book.Book;
import com.project.bookseller.exceptions.ResourceNotFoundException;
import com.project.bookseller.repository.book.BookRepository;
import com.project.bookseller.service.BookService;
import com.project.bookseller.service.elasticSearch.BookDocumentService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/random")
    public ResponseEntity<List<BookDTO>> getRandomBooks() {
        int a = 0;
        List<BookDTO> response = bookRepository.findAllBooksWithCategories().stream().map(BookDTO::convertFromBook).toList();
        List<BookDTO> randomBooks = new ArrayList<>();
        Random random = new Random();
        int b = response.size() - 1;
        while (randomBooks.size() < 10) {
            int randomInt = a + random.nextInt(b - a + 1);
            randomBooks.add(response.get(randomInt));
        }
        return ResponseEntity.ok(randomBooks);
    }

    @GetMapping("/{isbn}")
    public ResponseEntity<BookDTO> getBook(@PathVariable String isbn) {
        try {
            BookDTO book = bookService.findCompleteBook(isbn);
            return new ResponseEntity<>(book, HttpStatusCode.valueOf(200));
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatusCode.valueOf(404));
        }
    }

    @GetMapping("/index")
    public ResponseEntity<BookDocument> indexAllBooks() {
        List<Book> books = bookRepository.findAllBooks();
        for (Book book : books) {
            BookDocument bookDocument = new BookDocument();
            bookDocument.setId(String.valueOf(book.getBookId()));
            bookDocument.setTitle(book.getTitle());
            bookDocument.setBook_desc(book.getBookDesc());
            bookDocument.setPages(book.getPages());
            bookDocument.setPrice(book.getPrice());
            bookDocument.setPublisher(book.getPublisher());
            bookDocument.setBook_cover(book.getBookCover());
            bookDocument.setPublication_date(book.getPublicationDate());
            bookDocument.setIsbn(book.getIsbn());
            bookDocument.setAuthors(book.getAuthors().stream()
                    .map(AuthorDocument::convertFromEntity).toList());
            bookDocument.setCategories(book.getCategories().stream()
                    .map(CategoryDocument::convertFromEntity).toList());
            bookDocumentService.indexBookDocument(bookDocument);
        }
        return ResponseEntity.ok(null);
    }

    @GetMapping("search")
    public ResponseEntity<List<BookDocument>> searchByKeyword(
            @RequestParam String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) String sort_by,
            @RequestParam(required = false) String filter) {
        int currentPage = (page == null || page <= 0) ? 0 : page - 1;
        String fil = (filter == null || filter.isBlank()) ? null : filter;
        try {
            List<BookDocument> books = bookDocumentService
                    .searchByKeyword(keyword, currentPage, sort_by, fil);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatusCode.valueOf(500));
        }
    }

    @GetMapping("similar-author")
    public ResponseEntity<List<BookDocument>> getSimilarBooksByAuthor(@RequestParam String author) {
        try {
            List<BookDocument> books = bookDocumentService.getSimilarBooksByAuthor(author);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatusCode.valueOf(500));
        }
    }

    @GetMapping("similar")
    public ResponseEntity<List<BookDocument>> getSimilarBooks(@RequestParam String id) throws IOException {
        BookDocument bookDocument = bookDocumentService.getBookDocument(id);
        return ResponseEntity.ok(bookDocumentService.getMoreLikeThis(bookDocument));
    }
}
