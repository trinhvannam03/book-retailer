package com.project.bookseller.controller;

import com.project.bookseller.authentication.UserPrincipal;
import com.project.bookseller.dto.CartRecordDTO;
import com.project.bookseller.dto.order.OrderRecordDTO;
import com.project.bookseller.entity.order.OrderType;
import com.project.bookseller.exceptions.NotEnoughStockException;
import com.project.bookseller.exceptions.ResourceNotFoundException;
import com.project.bookseller.service.user.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    //add to cart
    @PostMapping("/")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<CartRecordDTO> addToCart(@AuthenticationPrincipal UserPrincipal userDetails, @RequestBody CartRecordDTO cartRecordDTO) {
        try {
            CartRecordDTO cartRecord = cartService.addToCart(userDetails, cartRecordDTO);
            return ResponseEntity.ok(cartRecord);
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
    ResponseEntity<List<CartRecordDTO>> getCart(@AuthenticationPrincipal UserPrincipal userDetails) {
        return new ResponseEntity<>(cartService.getCart(userDetails), HttpStatusCode.valueOf(200));
    }

    //delete from cart
    @PostMapping("/delete")
    ResponseEntity<List<CartRecordDTO>> deleteFromCart(@AuthenticationPrincipal UserPrincipal userDetails, @RequestBody List<Long> cartRecordIds) {
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
    ResponseEntity<CartRecordDTO> updateCart(@AuthenticationPrincipal UserPrincipal userDetails, @RequestBody CartRecordDTO cartRecordDTO) {
        Long cartRecordId = cartRecordDTO.getCartRecordId();
        Integer quantity = cartRecordDTO.getQuantity();
        try {
            CartRecordDTO cartRecord = cartService.updateCart(userDetails, cartRecordId, quantity);
            return ResponseEntity.ok(cartRecord);
        } catch (ResourceNotFoundException | NotEnoughStockException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
    }

    //check and return checked items when accessing checkout page.
    @PostMapping("/pre-check")
    ResponseEntity<List<OrderRecordDTO>> preCheck(@AuthenticationPrincipal UserPrincipal userDetails,
                                                  @RequestBody List<CartRecordDTO> cartRecordDTOs,
                                                  @RequestParam OrderType type
    ) {
        try {
            List<OrderRecordDTO> cartRecords = cartService.preCheck(userDetails, cartRecordDTOs, type);
            System.out.println(cartRecordDTOs);
            return ResponseEntity.ok(cartRecords);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
    }
}
