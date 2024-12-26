package com.project.bookseller.dto;

import com.project.bookseller.entity.order.OrderRecord;
import lombok.Data;

@Data
public class OrderRecordDTO {
    private long orderRecordId;
    private long bookId;
    private BookDTO book;
    private long cartRecordId;
    private double price;
    private int quantity;

    public static OrderRecordDTO convertFromEntity(OrderRecord orderRecord) {
        OrderRecordDTO dto = new OrderRecordDTO();
        dto.setQuantity(orderRecord.getQuantity());
        dto.setPrice(orderRecord.getPrice());
        dto.setOrderRecordId(orderRecord.getOrderRecordId());
        dto.setBook(BookDTO.convertFromBook(orderRecord.getBook()));
        return dto;
    }
}
