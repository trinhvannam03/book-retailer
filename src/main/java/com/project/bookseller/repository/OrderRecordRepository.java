package com.project.bookseller.repository;

import com.project.bookseller.entity.order.OrderRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRecordRepository extends JpaRepository<OrderRecord, Integer> {
}
