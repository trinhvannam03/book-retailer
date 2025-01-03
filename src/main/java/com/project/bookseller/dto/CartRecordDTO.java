package com.project.bookseller.dto;

import com.project.bookseller.dto.book.BookDTO;
import com.project.bookseller.entity.location.StockRecord;
import com.project.bookseller.entity.user.CartRecord;
import lombok.Data;

@Data
public class CartRecordDTO {
    private long id;
    private int quantity;
    private StockRecordDTO stockRecord;

    public static CartRecordDTO convertFromEntity(CartRecord cartRecord) {
        CartRecordDTO cartRecordDTO = new CartRecordDTO();
        cartRecordDTO.setId(cartRecord.getCartRecordId());
        cartRecordDTO.setQuantity(cartRecord.getQuantity());
        cartRecordDTO.setStockRecord(StockRecordDTO.convertFromEntity(cartRecord.getStockRecord()));
        return cartRecordDTO;
    }
}
