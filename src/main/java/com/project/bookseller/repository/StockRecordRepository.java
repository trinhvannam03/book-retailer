package com.project.bookseller.repository;

import com.project.bookseller.entity.location.LocationType;
import com.project.bookseller.entity.location.StockRecord;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRecordRepository extends JpaRepository<StockRecord, Integer> {

    //find a specific stock record
    @Query("SELECT r FROM StockRecord r JOIN FETCH r.book b WHERE r.stockRecordId = :stockRecordId")
    Optional<StockRecord> findStockRecordByStockRecordId(long stockRecordId);

    //find all locations for a book
    @Query("SELECT r FROM StockRecord r JOIN FETCH r.location l JOIN FETCH l.city c WHERE l.locationType = :locationType AND r.book.bookId = :bookId")
    List<StockRecord> findStockRecordsByBookIdAndLocationType(Long bookId, LocationType locationType);

    //find all books in a location
    @Query("SELECT r FROM StockRecord r JOIN FETCH r.book b WHERE b.bookId IN :bookIds AND r.location.locationId = :locationId")
    List<StockRecord> findStockRecordsByLocationIdAndBookIdIn(Long locationId, List<Long> bookIds, Sort sort);


    //when placing orders
    @Query("SELECT r FROM StockRecord r JOIN FETCH r.book b WHERE r.stockRecordId IN :stockRecordIds")
    List<StockRecord> findStockRecordsByStockRecordIdIn(List<Long> stockRecordIds);
}
