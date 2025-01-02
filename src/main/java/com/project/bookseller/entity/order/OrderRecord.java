package com.project.bookseller.entity.order;

import com.project.bookseller.entity.book.Book;
import com.project.bookseller.entity.location.StockRecord;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class OrderRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long orderRecordId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;
    private Double price;
    private Integer quantity;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_information_id")
    private Order orderInformation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_record_id")
    private StockRecord stockRecord;
}
