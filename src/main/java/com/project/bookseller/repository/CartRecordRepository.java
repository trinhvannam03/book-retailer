package com.project.bookseller.repository;

import com.project.bookseller.entity.user.CartRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRecordRepository extends JpaRepository<CartRecord, Integer> {


    Optional<CartRecord> findCartRecordByUser_UserIdAndBook_BookId(Long userId, Long bookId);

    @Query("SELECT c FROM CartRecord c " +
            "JOIN FETCH c.book b " +
            "JOIN FETCH b.stockRecords s " +
            "WHERE c.user.userId = :userId " +
            "AND s.location.locationType = 'ONLINE_STORE' " +
            "AND (:cartRecordIds IS NULL OR c.cartRecordId IN :cartRecordIds) " +
            "ORDER BY c.cartRecordId DESC")
    List<CartRecord> findCartRecordsWithStock(Long userId, List<Long> cartRecordIds);


    @Query("DELETE FROM CartRecord c WHERE c.cartRecordId IN :cartRecordIds")
    void deleteByCartRecordIds(List<Long> cartRecordIds);

}
