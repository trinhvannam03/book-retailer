package com.project.bookseller.repository;

import com.project.bookseller.entity.user.CartRecord;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CartRecordRepository extends JpaRepository<CartRecord, Integer> {

    @Query("SELECT c FROM CartRecord c JOIN FETCH c.stockRecord r WHERE r.stockRecordId = :stockRecordId AND c.user.userId = :userId")
    List<CartRecord> findCartRecordByUserIdAndStockRecordId(Long userId, Long stockRecordId);


    @Query("SELECT c FROM CartRecord c " +
            "JOIN FETCH c.stockRecord s " +
            "JOIN FETCH s.book b " +
            "WHERE (:cartRecordIds IS NULL OR c.cartRecordId IN :cartRecordIds) " +
            "ORDER BY c.cartRecordId DESC")
    List<CartRecord> findCartRecordsWithStock(Long userId, List<Long> cartRecordIds);


    void deleteAllByCartRecordIdIn(List<Long> cartRecordIds);

}
