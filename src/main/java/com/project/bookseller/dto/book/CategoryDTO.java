package com.project.bookseller.dto;

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
    private long parentId;
    private List<CategoryDTO> children;

    public static CategoryDTO convertFromCategory(Category category) {
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setCategoryId(category.getCategoryId());
        categoryDTO.setCategoryName(category.getCategoryName());
        categoryDTO.setCategoryDesc(category.getCategoryDesc());
        categoryDTO.setParentId(category.getParentId());
        List<CategoryDTO> children = new ArrayList<>();
        categoryDTO.setChildren(children);
        return categoryDTO;
    }
}
