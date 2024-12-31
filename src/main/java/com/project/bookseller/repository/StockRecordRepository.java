package com.project.bookseller.repository;

import com.project.bookseller.entity.location.StockRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRecordRepository extends JpaRepository<StockRecord, Integer> {
    Optional<StockRecord> findStockRecordByStockRecordId(long stockRecordId);
}
