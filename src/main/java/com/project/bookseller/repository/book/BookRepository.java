package com.project.bookseller.repository.book;

import com.project.bookseller.entity.book.Book;
import com.project.bookseller.entity.location.LocationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {


    @Query("SELECT b FROM Book b JOIN FETCH b.categories")
    List<Book> findAllBooks();

    @Query("SELECT b from Book b JOIN FETCH b.stockRecords r where b.bookId = :bookId and r.location.locationType = :locationType")
    Optional<Book> findBookWithStockRecords(Long bookId, LocationType locationType);

}

