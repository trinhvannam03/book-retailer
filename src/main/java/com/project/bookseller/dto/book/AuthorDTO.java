package com.project.bookseller.dto.book;

import com.project.bookseller.entity.book.Author;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AuthorDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -2392238704646996537L;
    private long authorId;
    private String authorName;
    private String authorDesc;

    public static AuthorDTO convertFromEntity(Author author) {
        AuthorDTO authorDTO = new AuthorDTO();
        authorDTO.setAuthorId(author.getAuthorId());
        authorDTO.setAuthorName(author.getAuthorName());
        authorDTO.setAuthorDesc(author.getAuthorDesc());
        return authorDTO;
    }
}
