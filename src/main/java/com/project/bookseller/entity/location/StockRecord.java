package com.project.bookseller.entity.location;

import com.project.bookseller.entity.book.Book;
import com.project.bookseller.entity.order.OrderRecord;
import com.project.bookseller.entity.user.CartRecord;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Data
public class StockRecord {
    @Version
    private Long version;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long stockRecordId;
    private int quantity;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @OneToMany(mappedBy = "stockRecord")
    private List<OrderRecord> orderRecords = new ArrayList<>();

    @OneToMany(mappedBy = "stockRecord", fetch = FetchType.LAZY)
    private List<CartRecord> cartRecords = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockRecord that = (StockRecord) o;
        return getStockRecordId() == that.getStockRecordId();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getStockRecordId());
    }


}
