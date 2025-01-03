package com.project.bookseller.dto.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.bookseller.dto.UserDTO;
import com.project.bookseller.dto.address.CityDTO;
import com.project.bookseller.dto.address.UserAddressDTO;
import com.project.bookseller.entity.order.*;
import com.project.bookseller.entity.user.address.City;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDTO {
    private Double estimatedShippingFee;
    private Long orderInformationId;
    private Double estimatedDiscount;
    private Double estimatedTotal;
    private String appliedCoupon;
    private PaymentInfo payment;
    private UserDTO user;
    private CityDTO city;
    private Long cityId;
    private String fullAddress;
    private String phone;
    private String email;
    private String fullName;
    private String longitude;
    private String latitude;
    private OrderType orderType;
    private PaymentMethod paymentMethod;
    private Long userId;
    private Double total;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime cancelledAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedAt;
    private OrderStatus orderStatus;
    private double discount = 0;
    private List<OrderRecordDTO> items = new ArrayList<>();

    public static OrderDTO convertFromEntity(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderStatus(order.getOrderStatus());
        dto.setDiscount(order.getDiscount());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setCancelledAt(order.getCancelledAt());
        dto.setCompletedAt(order.getCompletedAt());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setTotal(order.getTotal());
        dto.setOrderInformationId(order.getOrderInformationId());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setFullName(order.getFullName());
        dto.setLongitude(order.getLongitude());
        dto.setLatitude(order.getLatitude());
        dto.setPhone(order.getPhone());
        dto.setFullAddress(order.getFullAddress());
        return dto;
    }
}
