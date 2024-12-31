package com.project.bookseller.service.elasticSearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Like;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.*;

@RequiredArgsConstructor
@Service
public class BookDocumentService {
    private final ElasticsearchTemplate elasticsearchTemplate;
    private final ElasticsearchClient elasticsearchClient;
    private final BookDocumentRepository bookDocumentRepository;
    private final BookRepository bookRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ObjectMapper objectMapper;

    public void indexBookDocument(BookDocument book) {
        bookDocumentRepository.save(book);
    }

    public void deleteBookDocument(BookDocument book) throws RuntimeException {
        bookDocumentRepository.delete(book);
    }

    public void updateBookDocument(BookDocument book) throws RuntimeException {
        indexBookDocument(book);
    }

    public BookDocument getBookDocument(String book) throws RuntimeException {
        Optional<BookDocument> bookDocumentOptional = bookDocumentRepository.findById(book);
        if (bookDocumentOptional.isPresent()) {
            BookDocument bookDocument = bookDocumentOptional.get();
            return bookDocument;
        }
        return null;
    }

    public List<BookDocument> returnResult(SearchHits<BookDocument> searchHits) {
        List<BookDocument> bookDocuments = new ArrayList<>();
        for (SearchHit<BookDocument> searchHit : searchHits) {
            bookDocuments.add(searchHit.getContent());
        }
        return bookDocuments;
    }


    public List<BookDocument> searchByKeyword(String keyword, int page, String sortBy, String filter) throws IOException {

        co.elastic.clients.elasticsearch._types.query_dsl.Query dslQuery = co.elastic.clients.elasticsearch._types.query_dsl.Query.of(q -> q
                .bool(
                        b -> {
                            BoolQuery.Builder boolQuery = new BoolQuery.Builder();
                            boolQuery
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
                                            .boost(1.0F)));
                            if (filter != null) {
                                String[] fil = filter.split("_");
                                if (fil.length == 2) {
                                    String filterType = fil[0];
                                    String filterValue = fil[1];
                                    switch (filterType) {
                                        case "category": {
                                            //add category filter
                                            try {
                                                List<Long> filterContent = objectMapper
                                                        .readValue(filterValue, new TypeReference<>() {
                                                        });
                                                List<FieldValue> fieldTypes = filterContent.stream().map(FieldValue::of).toList();
                                                boolQuery = boolQuery.filter(f -> f.nested(ne -> ne.path("categories")
                                                        .query(qu -> qu.terms(t -> t.field("categories.category_id").terms(TermsQueryField.of(tqf -> tqf.value(fieldTypes))))  // Pass fieldTypes directly
                                                )));

                                            } catch (JsonProcessingException e) {
                                                throw new RuntimeException(e);
                                            } catch (RuntimeException e) {
                                                e.printStackTrace();
                                            }
                                            break;
                                        }
                                        case "price": {
                                            try {
                                                List<Double> filterContent = objectMapper
                                                        .readValue(filterValue, new TypeReference<>() {
                                                        });
                                                double gte = filterContent.get(0);
                                                double lte = filterContent.get(1);
                                                boolQuery = boolQuery.must(must -> must.range(r -> r.number(nu -> nu.field("price").gte(gte).lte(lte))));
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                throw new RuntimeException(e);
                                            }
                                            // Assuming filterContent is a price value (you might want to parse it if necessary)
                                            break;
                                        }
                                        default: {
                                            break;
                                        }
                                    }
                                }
                            }
                            return boolQuery;
                        }
                )
        );
        Query query = NativeQuery.builder().withQuery(dslQuery)
                .withPageable(Pageable.ofSize(8).withPage(page))
                .build();
        SearchHits<BookDocument> searchHits = elasticsearchOperations.search(query, BookDocument.class);
        List<BookDocument> results = returnResult(searchHits);
        Comparator<BookDocument> comparator = null;
        if (sortBy != null) {
            System.out.println(sortBy);
            switch (sortBy) {
                case "price_asc":
                    comparator = Comparator.comparing(BookDocument::getPrice);
                    break;
                case "price_desc":
                    comparator = Comparator.comparing(BookDocument::getPrice).reversed();
                    break;
                case "title_asc":
                    comparator = Comparator.comparing(BookDocument::getTitle);
                    break;
                case "title_desc":
                    comparator = Comparator.comparing(BookDocument::getTitle).reversed();
                    break;
                default:
                    break;
            }
            if (comparator != null) {
                results.sort(comparator);
            }
        }
        return results;
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

    public List<BookDocument> getMoreLikeThis(BookDocument document) throws IOException {
        List<String> fields = Arrays.asList("title", "book_desc");
        List<String> nestedFields = Arrays.asList("categories.category_name", "categories.category_desc");

        Like like = Like.of(l -> l.document(d -> d.doc(JsonData.of(document))));
        Query query = NativeQuery.builder().withQuery(
                qu -> qu.bool(b -> b
                        .should(s -> s.moreLikeThis(m -> m.fields(fields).like(like)))
                        .should(
                                s -> s.nested(n -> n.path("categories").query(q -> q.moreLikeThis(m -> m.like(like).fields(nestedFields))))
                        ).mustNot(mn -> mn.match(m -> m.field("title").query(document.getTitle()))))
        ).build();
        SearchHits<BookDocument> searchHits = elasticsearchOperations.search(query, BookDocument.class);
        return returnResult(searchHits);
    }
}
