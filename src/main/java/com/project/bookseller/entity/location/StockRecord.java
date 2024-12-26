package com.project.bookseller.entity.location;

import com.project.bookseller.entity.book.Book;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.Objects;

@Entity
@Data
public class StockRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long stockRecordId;

    @Min(0)
    private int quantity;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @Version
    private Integer version;

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
