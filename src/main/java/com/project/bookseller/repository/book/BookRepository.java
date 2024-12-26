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




    @Query("SELECT b from Book b JOIN FETCH b.stockRecords s where b.bookId = :bookId and s.location.locationType = 'ONLINE_STORE'")
    Optional<Book> findBriefBookByBookId(Long bookId);


    @Query("SELECT b FROM Book b " +
            "JOIN FETCH b.stockRecords s " +
            "WHERE b.bookId IN :bookIds AND s.location.locationType = 'ONLINE_STORE'")
    List<Book> findBriefBooksByBookIds(List<Long> bookIds);


    @Query("SELECT b from Book b JOIN FETCH b.categories")
    List<Book> findAllBooksWithCategories();

    Optional<Book> findBookByBookId(long bookId);

    @Query("SELECT b from Book b JOIN FETCH b.categories where b.isbn = :bookIsbn")
    Optional<Book> findBookByIsbnWithCategories(String bookIsbn);


    @Query("SELECT b from Book b " +
            "JOIN FETCH b.stockRecords r " +
            "JOIN FETCH r.location l " +
            "JOIN FETCH l.city c " +
            "JOIN FETCH c.state state " +
            "JOIN FETCH state.country where b.isbn = :isbn and l.locationType = :locationType")
    Optional<Book> findBookByIsbnWithStockRecordsAndLocationType(String isbn, LocationType locationType);


}

