package com.project.bookseller.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.bookseller.elasticSearchEntity.Book;
import com.project.bookseller.elasticSearchEntity.messageValue.ChangeOperation;
import com.project.bookseller.elasticSearchEntity.messageValue.KafkaMessageValue;
import com.project.bookseller.service.elasticSearch.ElasticSearchBookService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@EnableKafka
@RequiredArgsConstructor
public class DatabaseChangesListener {
    private final ElasticSearchBookService elasticSearchBookService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "bookchain.bookchain.book", containerFactory = "kafkaListenerContainerFactory")
    public void consumeDebeziumMessage(ConsumerRecord<String, Object> record) {
        System.out.println(record.value().toString());
    }

    public void processMessage(ConsumerRecord<String, String> record) {
        String value = record.value();
        KafkaMessageValue kafkaMessageValue = objectMapper.convertValue(value, KafkaMessageValue.class);
        Book before = kafkaMessageValue.getPayload().getBefore();
        Book after = kafkaMessageValue.getPayload().getAfter();
        ChangeOperation changeOperation = null;
        if (after == null) {
            changeOperation = ChangeOperation.DELETE;
        } else if (before == null) {
            changeOperation = ChangeOperation.CREATE;
        } else {
            changeOperation = ChangeOperation.UPDATE;
        }
    }
}
