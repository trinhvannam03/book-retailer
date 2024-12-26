package com.project.bookseller.dto;

import com.project.bookseller.entity.location.StockRecord;
import lombok.Data;

@Data
public class StockRecordDTO {
    private long stockRecordId;
    private int quantity;
    private LocationDTO store;
    private BookDTO book;
    public static StockRecordDTO convertFromStockRecord(StockRecord stockRecord) {
        StockRecordDTO stockRecordDTO = new StockRecordDTO();
        stockRecordDTO.setStockRecordId(stockRecord.getStockRecordId());
        stockRecordDTO.setQuantity(stockRecord.getQuantity());
        stockRecordDTO.setStore(LocationDTO.convertFromStore(stockRecord.getLocation()));
        return stockRecordDTO;
    }
}
