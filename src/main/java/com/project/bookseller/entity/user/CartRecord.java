package com.project.bookseller.entity.user;

import com.project.bookseller.entity.location.StockRecord;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(
        name = "cart_record",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "book_id"})
)
public class CartRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long cartRecordId;
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    @Column(name = "user_id")
    private long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_record_id", insertable = false, updatable = false)
    private StockRecord stockRecord;

    @Column(name = "stock_record_id")
    private long stockRecordId;
}
