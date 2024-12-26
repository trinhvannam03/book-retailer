package com.project.bookseller.service;


import com.project.bookseller.dto.CategoryDTO;
import com.project.bookseller.entity.book.Category;
import com.project.bookseller.repository.book.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    //HELPER
    public CategoryDTO getCategoriesAsTree(Category parent, List<Category> categories) {
        CategoryDTO categoryDTO = CategoryDTO.convertFromCategory(parent);
        for (Category category : categories) {
            if (category.getParentId() == parent.getCategoryId()) {
                categoryDTO.getChildren().add(getCategoriesAsTree(category, categories));
            }
        }
        return categoryDTO;
    }

    public CategoryDTO getRoot(Category parent, List<Category> categories, List<CategoryDTO> roots) {
        CategoryDTO categoryDTO = CategoryDTO.convertFromCategory(parent);
        for (Category category : categories) {
            if (category.getParentId() == parent.getCategoryId()) {
                categoryDTO.getChildren().add(getRoot(category, categories, roots));
            }
        }
        if (categoryDTO.getChildren().isEmpty()) {
            roots.add(categoryDTO);
        }
        return categoryDTO;
    }

    public List<CategoryDTO> getAllCategoriesAsDTOs() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryDTO> categoryDTOs = new ArrayList<>();
        for (Category category : categories) {
            if (category.getParentId() == 0) {
                categoryDTOs.add(getCategoriesAsTree(category, categories));
            }
        }
        return categoryDTOs;
    }

    public List<CategoryDTO> getAllRootCategoriesAsDTOs() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryDTO> categoryDTOs = new ArrayList<>();
        List<CategoryDTO> rootCategories = new ArrayList<>();
        for (Category category : categories) {
            if (category.getParentId() == 0) {
                categoryDTOs.add(getRoot(category, categories, rootCategories));
            }
        }
        return rootCategories;
    }
}
