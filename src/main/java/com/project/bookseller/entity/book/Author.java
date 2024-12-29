package com.project.bookseller.entity.book;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long authorId;
    private String authorName;
    private String authorDesc;
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "authors")
    private List<Book> books = new ArrayList<>();
}
