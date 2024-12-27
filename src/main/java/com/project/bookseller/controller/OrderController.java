package com.project.bookseller.controller;

import com.project.bookseller.authentication.UserDetails;
import com.project.bookseller.dto.order.OrderInformationDTO;
import com.project.bookseller.exceptions.DataMismatchException;
import com.project.bookseller.exceptions.NotEnoughStockException;
import com.project.bookseller.service.OrderService;
import com.project.bookseller.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {
    private final PaymentService paymentService;
    private final OrderService orderService;

    //create order
    @PostMapping("/")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<Map<String, Object>> createOrder(@RequestBody OrderInformationDTO info, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Map<String, Object> response = orderService.createOrder(userDetails, info);
            return new ResponseEntity<>(response, HttpStatusCode.valueOf(200));
        } catch (DataMismatchException | NotEnoughStockException e) {
            return ResponseEntity.badRequest().build();
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/")
    ResponseEntity<List<OrderInformationDTO>> getOrders(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.badRequest().build();
        }
        List<OrderInformationDTO> orders = orderService.getOrders(userDetails);
        return new ResponseEntity<>(orders, HttpStatusCode.valueOf(200));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{orderId}")
    ResponseEntity<OrderInformationDTO> getOrder(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long orderId) {
        if (orderId == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            OrderInformationDTO order = orderService.getOrder(userDetails, orderId);
            return new ResponseEntity<>(order, HttpStatusCode.valueOf(200));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
