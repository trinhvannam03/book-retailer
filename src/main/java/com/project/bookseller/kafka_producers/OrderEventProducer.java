package com.project.bookseller.kafka_producers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.bookseller.dto.order.OrderDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void emitOrderCreatedEvent(OrderDTO order) {
        try {
            String orderJson = objectMapper.writeValueAsString(order);
            kafkaTemplate.send("orders_topic", UUID.randomUUID().toString(), orderJson);
            System.out.println("Produced order event: " + orderJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
