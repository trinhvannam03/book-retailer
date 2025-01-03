package com.project.bookseller.elasticSearchEntity;

import com.project.bookseller.entity.book.BookDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookDocumentRepository extends ElasticsearchRepository<BookDocument, String> {
}
