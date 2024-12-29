package com.project.bookseller.elasticSearchEntity;

import com.project.bookseller.entity.book.Category;
import lombok.Data;

@Data
public class CategoryDocument {
    private long category_id;
    private String category_name;
    private String category_desc;

    public static CategoryDocument convertFromEntity(Category category) {
        CategoryDocument categoryDocument = new CategoryDocument();
        categoryDocument.setCategory_id(category.getCategoryId());
        categoryDocument.setCategory_name(category.getCategoryName());
        categoryDocument.setCategory_desc(category.getCategoryDesc());
        return categoryDocument;
    }
}
