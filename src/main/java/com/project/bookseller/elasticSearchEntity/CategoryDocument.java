package com.project.bookseller.elasticSearchEntity;

import com.project.bookseller.entity.book.Category;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
public class CategoryDocument {
    @Field(type = FieldType.Text)
    private long category_id;
    @Field(type = FieldType.Text)
    private String category_name;
    @Field(type = FieldType.Text)
    private String category_desc;

    public static CategoryDocument convertFromEntity(Category category) {
        CategoryDocument categoryDocument = new CategoryDocument();
        categoryDocument.setCategory_id(category.getCategoryId());
        categoryDocument.setCategory_name(category.getCategoryName());
        categoryDocument.setCategory_desc(category.getCategoryDesc());
        return categoryDocument;
    }
}
