package com.project.bookseller.dto.book;

import com.project.bookseller.entity.book.Category;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -5710000495448905777L;
    private long categoryId;
    private String categoryName;
    private String categoryDesc;

    public static CategoryDTO convertFromCategory(Category category) {
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setCategoryId(category.getCategoryId());
        categoryDTO.setCategoryName(category.getCategoryName());
        categoryDTO.setCategoryDesc(category.getCategoryDesc());
        return categoryDTO;
    }
}
