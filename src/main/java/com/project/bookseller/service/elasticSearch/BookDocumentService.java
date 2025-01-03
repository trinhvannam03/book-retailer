package com.project.bookseller.service.elasticSearch;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Like;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import co.elastic.clients.json.JsonData;
import com.project.bookseller.dto.StockRecordDTO;
import com.project.bookseller.dto.book.BookDTO;
import com.project.bookseller.elasticSearchEntity.BookDocumentRepository;
import com.project.bookseller.entity.book.BookDocument;
import com.project.bookseller.entity.location.Location;
import com.project.bookseller.entity.location.LocationType;
import com.project.bookseller.entity.location.StockRecord;
import com.project.bookseller.repository.LocationRepository;
import com.project.bookseller.repository.StockRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final BookDocumentRepository bookDocumentRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final StockRecordRepository stockRecordRepository;
    private final LocationRepository locationRepository;

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

    public List<StockRecordDTO> returnResult(Long locationId, SearchHits<BookDocument> searchHits, Sort sort) {
        if (locationId == null) {
            List<Location> locations = locationRepository.findLocationsByLocationType(LocationType.ONLINE_STORE);
            assert locations.size() == 1;
            locationId = locations.get(0).getLocationId();
        }
        System.out.println(locationId);
        List<Long> bookDocuments = new ArrayList<>();
        for (SearchHit<BookDocument> searchHit : searchHits) {
            bookDocuments.add(Long.valueOf(Objects.requireNonNull(searchHit.getId())));
        }
        List<StockRecord> stockRecords = stockRecordRepository.findStockRecordsByLocationIdAndBookIdIn(locationId, bookDocuments, sort);
        return stockRecords.stream().map(r -> {
            StockRecordDTO stockRecordDTO = StockRecordDTO.convertFromEntity(r);
            stockRecordDTO.setBook(BookDTO.convertFromEntity(r.getBook()));
            return stockRecordDTO;
        }).toList();
    }


    public List<StockRecordDTO> searchByKeyword(Long locationId, String keyword, int page, String
            sortBy, List<String> categories, Double price_gte, Double price_lte) throws IOException {
        co.elastic.clients.elasticsearch._types.query_dsl.Query dslQuery = co.elastic.clients.elasticsearch._types.query_dsl.Query.of(q -> q.bool(b -> {
            BoolQuery.Builder boolQuery = new BoolQuery.Builder();
            boolQuery.should(sh -> sh.match(m -> m.field("title").query(keyword).fuzziness("AUTO").boost(20.0F))).should(s -> s.match(m -> m.field("book_desc").query(keyword).fuzziness("AUTO").boost(3.0F))).should(s -> s.nested(n -> n.path("authors").query(qu -> qu.match(m -> m  // Apply match query inside the nested query
                    .field("authors.author_name")  // Match on the author_name field within the nested object
                    .query(keyword)  // Search for the keyword
                    .fuzziness("AUTO")  // Use fuzziness for auto suggestion
                    .boost(3.0F))))).should(s -> s.nested(n -> n.path("categories").query(qry -> qry.match(m -> m.field("categories.category_name").query(keyword).fuzziness("AUTO").boost(2.0F)))));
            if (categories != null && !categories.isEmpty()) {
                System.out.println(categories.toString());
                List<FieldValue> fieldValues = categories.stream().map(c -> FieldValue.of(String.valueOf(c))).toList();

                boolQuery = boolQuery.must(mu -> mu.nested(ne -> ne.path("categories").query(qu -> qu.bool(bq -> bq.must(m -> m.terms(t -> t.field("categories.category_id").terms(TermsQueryField.of(tqf -> tqf.value(fieldValues)))))))));
            }
            if (price_gte != null && price_gte > 0.0D) {
                boolQuery = boolQuery.must(must -> must.range(r -> r.number(nu -> nu.field("price").gte(price_gte))));
            }
            if (price_lte != null && price_lte > 0.0D) {
                boolQuery = boolQuery.must(must -> must.range(r -> r.number(nu -> nu.field("price").lt(price_lte))));
            }
            return boolQuery;
        }));
        Query query = NativeQuery.builder().withQuery(dslQuery).withPageable(Pageable.ofSize(8).withPage(page)).build();
        SearchHits<BookDocument> searchHits = elasticsearchOperations.search(query, BookDocument.class);

        Sort sort = null;
        if (sortBy != null) {
            System.out.println("SortBy:: " + sortBy);
            switch (sortBy) {
                case "price_asc":
                    sort = Sort.by(Sort.Direction.ASC, "b.price");
                    break;
                case "price_desc":
                    sort = Sort.by(Sort.Direction.DESC, "b.price");
                    break;
                case "title_asc":
                    sort = Sort.by(Sort.Direction.ASC, "b.title");
                    break;
                case "title_desc":
                    sort = Sort.by(Sort.Direction.DESC, "b.title");
                    break;
                default:
                    break;
            }
        }
        return returnResult(locationId, searchHits, sort);
    }

    public List<StockRecordDTO> getSimilarBooksByAuthor(Long locationId, String keyword) throws IOException {
        List<String> fields = Arrays.asList("authors.author_name", "author.author_id");
        Like like = Like.of(l -> l.text(keyword));
        // Build the query
        Query query = NativeQuery.builder().withQuery(q -> q.bool(b -> b.should(s -> s.nested(n -> n.path("authors").query(qu -> qu.match(m -> m  // Apply match query inside the nested query
                .field("authors.author_name")  // Match on the author_name field within the nested object
                .query(keyword)  // Search for the keyword
                .fuzziness("AUTO")  // Use fuzziness for auto suggestion
                .boost(5.0F))))).should(s -> s.nested(n -> n.path("categories").query(qry -> qry.match(m -> m.field("categories.category_name").query(keyword).fuzziness("AUTO").boost(2.0F))))))).build();
        SearchHits<BookDocument> searchHits = elasticsearchOperations.search(query, BookDocument.class);
        return returnResult(locationId, searchHits, null);
    }

    public List<StockRecordDTO> getMoreLikeThis(Long locationId, BookDocument document) throws IOException {
        List<String> fields = Arrays.asList("title", "book_desc");
        List<String> nestedFields = Arrays.asList("categories.category_name", "categories.category_desc");

        Like like = Like.of(l -> l.document(d -> d.doc(JsonData.of(document))));
        Query query = NativeQuery.builder().withQuery(qu -> qu.bool(b -> b.should(s -> s.moreLikeThis(m -> m.fields(fields).like(like))).should(s -> s.nested(n -> n.path("categories").query(q -> q.moreLikeThis(m -> m.like(like).fields(nestedFields))))).mustNot(mn -> mn.match(m -> m.field("title").query(document.getTitle()))))).build();
        SearchHits<BookDocument> searchHits = elasticsearchOperations.search(query, BookDocument.class);

        return returnResult(locationId, searchHits, null);
    }
}
