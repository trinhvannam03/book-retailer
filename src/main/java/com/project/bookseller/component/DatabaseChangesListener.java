package com.project.bookseller.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.project.bookseller.dto.book.BookDTO;
import com.project.bookseller.elasticSearchEntity.BookDocument;
import com.project.bookseller.service.elasticSearch.BookDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@EnableKafka
@RequiredArgsConstructor
public class DatabaseChangesListener {
    private final BookDocumentService bookDocumentService;

    @KafkaListener(topics = "bookchain.bookchain.book", containerFactory = "kafkaListenerContainerFactory")
    public void receiveDebeziumMessage(String message) {
        processDebeziumMessage(message);
    }

    public void processDebeziumMessage(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            JsonNode rootNode = objectMapper.readTree(message);
            JsonNode payloadNode = objectMapper.readTree(rootNode.get("payload").toString());
            JsonNode beforeJsonNode = payloadNode.get("before");
            JsonNode afterJsonNode = payloadNode.get("after");
            //delete
            if (afterJsonNode.isNull()) {
                if (beforeJsonNode.isNull()) {
                    throw new RuntimeException();
                }
                BookDTO before = objectMapper.treeToValue(beforeJsonNode, BookDTO.class);
                BookDocument book = BookDocument.convertFromDTO(before);
                bookDocumentService.deleteBookDocument(book);
            }
            //create
            else if (beforeJsonNode.isNull()) {
                BookDocument book = new BookDocument();
                bookDocumentService.indexBookDocument(book);

            }
            //update
            else {
                BookDTO after = objectMapper.treeToValue(afterJsonNode, BookDTO.class);
                BookDocument book = BookDocument.convertFromDTO(after);
                bookDocumentService.updateBookDocument(book);
            } //update
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
