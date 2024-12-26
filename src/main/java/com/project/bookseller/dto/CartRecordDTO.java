package com.project.bookseller.dto;

import com.project.bookseller.dto.book.BookDTO;
import com.project.bookseller.entity.user.CartRecord;
import lombok.Data;

@Data
public class CartRecordDTO {
    private long id;
    private int quantity;
    private BookDTO book;


    public static CartRecordDTO fromCartRecord(CartRecord cartRecord) {
        CartRecordDTO cartRecordDTO = new CartRecordDTO();
        cartRecordDTO.setId(cartRecord.getCartRecordId());
        cartRecordDTO.setQuantity(cartRecord.getQuantity());
        BookDTO bookDTO = BookDTO.convertFromBook(cartRecord.getBook());
        cartRecordDTO.setBook(bookDTO);
        return cartRecordDTO;
    }
}
