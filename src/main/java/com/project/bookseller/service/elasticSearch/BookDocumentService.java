package com.project.bookseller.service.elasticSearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Like;
import com.project.bookseller.elasticSearchEntity.BookDocument;
import com.project.bookseller.elasticSearchEntity.BookDocumentRepository;
import com.project.bookseller.repository.book.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Service
public class BookDocumentService {
    private final ElasticsearchTemplate elasticsearchTemplate;
    private final ElasticsearchClient elasticsearchClient;
    private final BookDocumentRepository bookDocumentRepository;
    private final BookRepository bookRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    public void indexBookDocument(BookDocument book) {
        bookDocumentRepository.save(book);
    }

    public void deleteBookDocument(BookDocument book) throws RuntimeException {
        bookDocumentRepository.delete(book);
    }

    public void updateBookDocument(BookDocument book) throws RuntimeException {
        indexBookDocument(book);
    }
    public List<BookDocument> returnResult(SearchHits<BookDocument> searchHits) {
        List<BookDocument> bookDocuments = new ArrayList<>();
        for (SearchHit<BookDocument> searchHit : searchHits) {
            bookDocuments.add(searchHit.getContent());
        }
        return bookDocuments;
    }

    public List<BookDocument> searchByKeyword(String keyword) throws IOException {
        Query query = NativeQuery.builder().withQuery(q -> q.bool(b -> b
                        .should(sh -> sh.match(m -> m.field("title").query(keyword).fuzziness("AUTO").boost(20.0F)))
                        .should(s -> s.match(m -> m.field("book_desc").query(keyword).fuzziness("AUTO").boost(3.0F)))
                        .should(s -> s.nested(n -> n.path("authors").query(qu -> qu.match(m -> m  // Apply match query inside the nested query
                                .field("authors.author_name")  // Match on the author_name field within the nested object
                                .query(keyword)  // Search for the keyword
                                .fuzziness("AUTO")  // Use fuzziness for auto suggestion
                                .boost(2.0F)))))
                        .should(s -> s.nested(n -> n.path("categories").query(qry -> qry.match(m -> m
                                .field("categories.category_name")
                                .query(keyword).fuzziness("AUTO")
                                .boost(2.0F)))))
                        .should(s -> s.match(m -> m.field("title")
                                .query(keyword.charAt(0))
                                .fuzziness("AUTO")
                                .boost(1.0F)))
                ))
                .withPageable(Pageable.ofSize(12))
                .build();
        SearchHits<BookDocument> searchHits = elasticsearchOperations.search(query, BookDocument.class);
        return returnResult(searchHits);
    }

    public List<BookDocument> getSimilarBooksByAuthor(String keyword) throws IOException {
        List<String> fields = Arrays.asList("authors.author_name", "author.author_id");
        Like like = Like.of(l -> l.text(keyword));
        // Build the query
        Query query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                        .should(s -> s.nested(n -> n.path("authors").query(qu -> qu.match(m -> m  // Apply match query inside the nested query
                                .field("authors.author_name")  // Match on the author_name field within the nested object
                                .query(keyword)  // Search for the keyword
                                .fuzziness("AUTO")  // Use fuzziness for auto suggestion
                                .boost(5.0F)))))
                        .should(s -> s.nested(n -> n.path("categories").query(qry -> qry.match(m -> m
                                .field("categories.category_name")
                                .query(keyword).fuzziness("AUTO")
                                .boost(2.0F)))))))
                .build();
        SearchHits<BookDocument> searchHits = elasticsearchOperations.search(query, BookDocument.class);
        return returnResult(searchHits);
    }
}
