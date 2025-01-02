package com.project.bookseller.entity.book;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long categoryId;
    private String categoryName;
    private String categoryDesc;
    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    private List<Book> books;
}
