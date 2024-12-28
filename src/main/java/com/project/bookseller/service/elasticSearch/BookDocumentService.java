package com.project.bookseller.service.elasticSearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.project.bookseller.elasticSearchEntity.BookDocument;
import com.project.bookseller.elasticSearchEntity.BookDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BookDocumentService {
    private final ElasticsearchTemplate elasticsearchTemplate;
    private final ElasticsearchClient client;
    private final BookDocumentRepository bookDocumentRepository;

    //actions with documents
    public void indexBookDocument(BookDocument book) {
        try {
            IndexRequest<BookDocument> indexRequest = new IndexRequest.Builder<BookDocument>()
                    .index("books")
                    .id(book.getId())
                    .document(book)
                    .build();
            client.index(indexRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteBookDocument(BookDocument book) throws RuntimeException {
        bookDocumentRepository.delete(book);
    }

    public void updateBookDocument(BookDocument book) throws RuntimeException {
        bookDocumentRepository.save(book);
    }

    public BookDocument getBookDocument(String id) throws RuntimeException {
        BookDocument book = bookDocumentRepository.findById(id).orElse(null);
        if (book == null) {
            throw new RuntimeException("Book not found");
        }
        return book;
    }

    //search operations

}
