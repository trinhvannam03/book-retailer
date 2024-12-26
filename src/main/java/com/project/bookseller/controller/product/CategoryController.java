package com.project.bookseller.controller.product;

import com.project.bookseller.dto.book.CategoryDTO;
import com.project.bookseller.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/")

    public List<CategoryDTO> getCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategoriesAsDTOs();
        return categories;
    }
}
