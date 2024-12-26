package com.project.bookseller.controller.product;

import com.project.bookseller.dto.BookDTO;
import com.project.bookseller.exceptions.ResourceNotFoundException;
import com.project.bookseller.repository.book.BookRepository;
import com.project.bookseller.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/books/")
@RequiredArgsConstructor
public class BookController {
    private final BookRepository bookRepository;
    private final BookService bookService;


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
}
