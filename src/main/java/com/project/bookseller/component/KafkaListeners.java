package com.project.bookseller.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.project.bookseller.dto.StockRecordDTO;
import com.project.bookseller.dto.address.CityDTO;
import com.project.bookseller.dto.book.BookDTO;
import com.project.bookseller.dto.order.OrderDTO;
import com.project.bookseller.dto.order.OrderRecordDTO;
import com.project.bookseller.entity.book.BookDocument;
import com.project.bookseller.entity.location.StockRecord;
import com.project.bookseller.entity.order.Order;
import com.project.bookseller.entity.order.OrderRecord;
import com.project.bookseller.entity.user.CartRecord;
import com.project.bookseller.entity.user.User;
import com.project.bookseller.entity.user.address.City;
import com.project.bookseller.repository.CartRecordRepository;
import com.project.bookseller.repository.OrderRecordRepository;
import com.project.bookseller.repository.OrderRepository;
import com.project.bookseller.service.elasticSearch.BookDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@EnableKafka
@RequiredArgsConstructor
public class KafkaListeners {
    private final ObjectMapper objectMapper;
    private final BookDocumentService bookDocumentService;
    private final OrderRepository orderRepository;
    private final OrderRecordRepository orderRecordRepository;
    private final CartRecordRepository cartRecordRepository;

    @KafkaListener(topics = "orders_topic", groupId = "email_senders")
    public void sendEmail(String message) {
        System.out.println("Message: " + message);
    }

    @KafkaListener(topics = "orders_topic", groupId = "side_task_performers")
    public void performSideTasks(String message) {
        try {
            OrderDTO orderDTO = objectMapper.readValue(message, OrderDTO.class);
            CityDTO cityDTO = orderDTO.getCity();
            City city = new City();
            city.setCityId(cityDTO.getId());
            city.setCityName(cityDTO.getName());
            List<OrderRecordDTO> items = orderDTO.getItems();
            List<OrderRecord> orderRecords = new ArrayList<>();
            List<Long> cartRecordIds = new ArrayList<>();
            User user = new User();
            user.setUserId(orderDTO.getUser().getUserId());
            Order order = new Order();
            order.setTotal(orderDTO.getTotal());
            order.setFullName(orderDTO.getFullName());
            order.setFullAddress(orderDTO.getFullAddress());
            order.setCreatedAt(LocalDateTime.now());
            order.setPhone(orderDTO.getPhone());
            order.setLongitude(orderDTO.getLongitude());
            order.setLatitude(orderDTO.getLatitude());
            order.setCity(city);
            order.setOrderType(orderDTO.getOrderType());
            order.setOrderRecords(orderRecords);
            order.setOrderStatus(orderDTO.getOrderStatus());
            order.setUser(user);
            order.setPaymentMethod(orderDTO.getPaymentMethod());
            for (OrderRecordDTO item : items) {
                OrderRecord orderRecord = new OrderRecord();
                StockRecordDTO stockRecordDTO = item.getStockRecord();
                StockRecord stockRecord = new StockRecord();
                stockRecord.setVersion(10L);
                stockRecord.setStockRecordId(stockRecordDTO.getStockRecordId());
                orderRecord.setStockRecord(stockRecord);
                orderRecord.setQuantity(item.getQuantity());
                orderRecord.setPrice(stockRecordDTO.getBook().getPrice());
                orderRecord.setOrderInformation(order);
                orderRecords.add(orderRecord);
                CartRecord cartRecord = new CartRecord();
                cartRecord.setCartRecordId(item.getCartRecordId());
                cartRecordRepository.delete(cartRecord);
            }

            orderRepository.save(order);
            orderRecordRepository.saveAll(orderRecords);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

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
                BookDTO after = objectMapper.treeToValue(afterJsonNode, BookDTO.class);
                BookDocument book = BookDocument.convertFromDTO(after);
                bookDocumentService.indexBookDocument(book);
            }
            //update
            else {
                BookDTO after = objectMapper.treeToValue(afterJsonNode, BookDTO.class);
                BookDocument book = BookDocument.convertFromDTO(after);
                System.out.println(book);
                bookDocumentService.updateBookDocument(book);
            } //update
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
