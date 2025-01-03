package com.project.bookseller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.bookseller.dto.book.BookDTO;
import com.project.bookseller.entity.location.StockRecord;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StockRecordDTO {
    private long stockRecordId;
    private int quantity;
    private LocationDTO location;
    private BookDTO book;

    public static StockRecordDTO convertFromEntity(StockRecord stockRecord) {
        StockRecordDTO stockRecordDTO = new StockRecordDTO();
        stockRecordDTO.setStockRecordId(stockRecord.getStockRecordId());
        stockRecordDTO.setQuantity(stockRecord.getQuantity());
        return stockRecordDTO;
    }
}
