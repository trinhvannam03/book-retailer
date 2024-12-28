package com.project.bookseller.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.project.bookseller.dto.book.BookDTO;
import com.project.bookseller.elasticSearchEntity.messageValue.ChangeOperation;
import com.project.bookseller.elasticSearchEntity.messageValue.KafkaMessageValue;
import com.project.bookseller.entity.book.Book;
import com.project.bookseller.service.elasticSearch.ElasticSearchBookService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

@Component
@EnableKafka
@RequiredArgsConstructor
public class DatabaseChangesListener {
    private final ElasticSearchBookService elasticSearchBookService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "bookchain.bookchain.book", containerFactory = "kafkaListenerContainerFactory")
    public void consumeDebeziumMessage(String message) {
        processDebeziumMessage(message);
    }


    public void processDebeziumMessage(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            JsonNode rootNode = objectMapper.readTree(message);
            String payloadString = rootNode.get("payload").toString();
            JsonNode payloadNode = objectMapper.readTree(payloadString);
            System.out.println(payloadNode.get("before"));
            JsonNode after = payloadNode.get("after");
            BookDTO book = objectMapper.treeToValue(after, BookDTO.class);
            System.out.println(book.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
