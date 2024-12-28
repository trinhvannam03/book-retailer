package com.project.bookseller.service.elasticSearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.project.bookseller.elasticSearchEntity.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ElasticSearchBookService {
    private final ElasticsearchTemplate elasticsearchTemplate;
    private final ElasticsearchClient client;

    public void indexBook(Book book) {
        try {
            IndexRequest<Book> indexRequest = new IndexRequest.Builder<Book>()
                    .index("products")
                    .id(book.getBookId())
                    .document(book)
                    .build();
            client.index(indexRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
