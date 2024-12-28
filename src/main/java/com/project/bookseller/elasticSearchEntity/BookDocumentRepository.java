package com.project.bookseller.elasticSearchEntity;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookDocumentRepository extends ElasticsearchRepository<BookDocument, String> {
}
