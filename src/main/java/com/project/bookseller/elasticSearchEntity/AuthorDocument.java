package com.project.bookseller.elasticSearchEntity;

import com.project.bookseller.entity.book.Author;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
public class AuthorDocument {
    @Field(type = FieldType.Text)
    private long author_id;

    @Field(type = FieldType.Text)
    private String author_name;

    @Field(type = FieldType.Text)
    private String author_desc;

    public static AuthorDocument convertFromEntity(Author author) {
        AuthorDocument authorDocument = new AuthorDocument();
        authorDocument.setAuthor_id(author.getAuthorId());
        authorDocument.setAuthor_name(author.getAuthorName());
        authorDocument.setAuthor_desc(author.getAuthorDesc());
        return authorDocument;
    }
}
