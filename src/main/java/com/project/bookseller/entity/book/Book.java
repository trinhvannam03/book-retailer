package com.project.bookseller.entity.book;

import com.project.bookseller.entity.user.CartRecord;
import com.project.bookseller.entity.order.OrderRecord;
import com.project.bookseller.entity.location.StockRecord;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long bookId;
    private String isbn;
    private String bookDesc;
    private String title;
    private String bookCover;
    private Integer bookWidth;
    private Integer bookHeight;
    private Integer bookLength;
    private Integer bookWeight;
    private String publisher;
    private Integer pages;
    private LocalDateTime publicationDate;
    private Double price;
    private String coverType;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_category",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories;
    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private List<CartRecord> cartRecords = new ArrayList<>();
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "book_author",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )

    private List<Author> authors = new ArrayList<>();

    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private List<StockRecord> stockRecords = new ArrayList<>();

    @Transient
    private int stock;

    @OneToMany(mappedBy = "book")
    List<OrderRecord> orderRecords = new ArrayList<>();

}
