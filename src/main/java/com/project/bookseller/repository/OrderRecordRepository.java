package com.project.bookseller.repository;

import com.project.bookseller.entity.order.OrderInformation;
import com.project.bookseller.entity.order.OrderRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRecordRepository extends JpaRepository<OrderRecord, Integer> {
}
