package com.project.bookseller.controller;

import com.project.bookseller.authentication.UserDetails;
import com.project.bookseller.dto.OrderInformationDTO;
import com.project.bookseller.exceptions.DataMismatchException;
import com.project.bookseller.exceptions.NotEnoughStockException;
import com.project.bookseller.service.OrderService;
import com.project.bookseller.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {
    private final PaymentService paymentService;
    private final OrderService orderService;

    @PostMapping("/")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<Map<String, Object>> placeOrder(@RequestBody OrderInformationDTO info, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Map<String, Object> response = orderService.createOrder(userDetails, info);
            return new ResponseEntity<>(response, HttpStatusCode.valueOf(200));
        } catch (DataMismatchException | NotEnoughStockException e) {
            return ResponseEntity.badRequest().build();
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
