package com.project.bookseller.service;


import com.project.bookseller.dto.book.CategoryDTO;
import com.project.bookseller.entity.book.Category;
import com.project.bookseller.repository.book.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
}
