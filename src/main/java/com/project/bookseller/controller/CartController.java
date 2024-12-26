package com.project.bookseller.controller;

import com.project.bookseller.authentication.UserDetails;
import com.project.bookseller.dto.CartRecordDTO;
import com.project.bookseller.exceptions.ResourceNotFoundException;
import com.project.bookseller.exceptions.NotEnoughStockException;
import com.project.bookseller.repository.book.BookRepository;
import com.project.bookseller.repository.CartRecordRepository;
import com.project.bookseller.service.BookService;
import com.project.bookseller.service.CartService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartRecordRepository cartRecordRepository;
    private final BookRepository bookRepository;
    private final CartService cartService;
    private final BookService bookService;

    @Data
    public static class RequestData {
        private String isbn;
        private int quantity;
        private Long cart_record_id;
        private Long book_id;
    }
    //add to cart
    @PostMapping("/")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<List<CartRecordDTO>> addToCart(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, Object> requestData) {
        try {
            cartService.addToCart(userDetails, ((Integer) requestData.get("book_id")).longValue(), (Integer) requestData.get("quantity"));
            List<CartRecordDTO> cartRecords = cartService.getCart(userDetails);
            return ResponseEntity.ok(cartRecords);
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatusCode.valueOf(404));
        } catch (NotEnoughStockException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
    }

    //get items in cart
    @GetMapping("/")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<List<CartRecordDTO>> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        return new ResponseEntity<>(cartService.getCart(userDetails), HttpStatusCode.valueOf(200));
    }

    //delete from cart
    @PostMapping("/delete")
    ResponseEntity<List<CartRecordDTO>> deleteFromCart(@AuthenticationPrincipal UserDetails userDetails, @RequestBody List<Long> cartRecordIds) {
        try {
            cartService.deleteFromCart(userDetails, cartRecordIds);
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
        return ResponseEntity.ok(cartService.getCart(userDetails));
    }

    //update quantity in cart
    @PutMapping("/update")
    ResponseEntity<CartRecordDTO> updateCart(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, Object> requestData) {
        Long cartRecordId = ((Integer) requestData.get("cart_record_id")).longValue();
        Integer quantity = (Integer) requestData.get("quantity");
        try {
            CartRecordDTO cartRecordDTO = cartService.updateCart(userDetails, cartRecordId, quantity);
            return ResponseEntity.ok(cartRecordDTO);
        } catch (ResourceNotFoundException | NotEnoughStockException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
    }


    //check and return checked items when accessing checkout page.
    @PostMapping("/checked-items")
    ResponseEntity<List<CartRecordDTO>> getCheckedItems(@AuthenticationPrincipal UserDetails userDetails, @RequestBody List<RequestData> requestData) {
        try {
            List<CartRecordDTO> cartRecordDTOs = cartService.getCheckedItems(userDetails, requestData);
            return ResponseEntity.ok(cartRecordDTOs);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
    }
}
