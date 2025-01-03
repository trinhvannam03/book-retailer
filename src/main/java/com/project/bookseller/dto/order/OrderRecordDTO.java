package com.project.bookseller.dto.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.bookseller.dto.StockRecordDTO;
import com.project.bookseller.dto.book.BookDTO;
import com.project.bookseller.entity.order.OrderRecord;
import lombok.Data;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderRecordDTO {
    private Long orderRecordId;
    private Double price;
    private Integer quantity;
    private StockRecordDTO stockRecord;
    private Long cartRecordId;

    public static OrderRecordDTO convertFromEntity(OrderRecord orderRecord) {
        OrderRecordDTO dto = new OrderRecordDTO();
        dto.setQuantity(orderRecord.getQuantity());
        dto.setPrice(orderRecord.getPrice());
        dto.setOrderRecordId(orderRecord.getOrderRecordId());
        return dto;
    }
}
