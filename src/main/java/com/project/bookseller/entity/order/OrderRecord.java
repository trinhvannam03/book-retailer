package com.project.bookseller.entity.order;

import com.project.bookseller.dto.order.OrderRecordDTO;
import com.project.bookseller.entity.book.Book;
import com.project.bookseller.entity.location.StockRecord;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class OrderRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderRecordId;
    private Double price;
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "book_id", insertable = false, updatable = false)
    private Book book;

    @Column(name = "book_id")
    private Long bookId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_information_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_record_id", insertable = false, updatable = false)
    private StockRecord stockRecord;

    @Column(name = "stock_record_id")
    private Long stockRecordId;

}
