package com.project.bookseller.elasticSearchEntity;

import com.project.bookseller.entity.book.Author;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
public class AuthorDocument {
    private long author_id;
    private String author_name;
    private String author_desc;

    public static AuthorDocument convertFromEntity(Author author) {
        AuthorDocument authorDocument = new AuthorDocument();
        authorDocument.setAuthor_id(author.getAuthorId());
        authorDocument.setAuthor_name(author.getAuthorName());
        authorDocument.setAuthor_desc(author.getAuthorDesc());
        return authorDocument;
    }
}
