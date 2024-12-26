package com.project.bookseller.entity.user;

import com.project.bookseller.entity.book.Book;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CartRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long cartRecordId;
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;
}
