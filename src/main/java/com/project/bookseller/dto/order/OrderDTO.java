package com.project.bookseller.dto.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.bookseller.dto.address.UserAddressDTO;
import com.project.bookseller.entity.order.Order;
import com.project.bookseller.entity.order.OrderRecord;
import com.project.bookseller.entity.order.OrderStatus;
import com.project.bookseller.entity.order.PaymentMethod;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDTO {
    private Double estimatedShippingFee;
    private long orderInformationId;
    private Double estimatedDiscount;
    private Double estimatedTotal;
    private String appliedCoupon;
    private UserAddressDTO address;
    private PaymentMethod paymentMethod;
    private PaymentMethodDTO payment;
    private double total;
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
        UserAddressDTO userAddressDTO = new UserAddressDTO();
        userAddressDTO.setFullAddress(order.getFullAddress());
        dto.setAddress(userAddressDTO);
        for (OrderRecord orderRecord : order.getOrderRecords()) {
            dto.getItems().add(OrderRecordDTO.convertFromEntity(orderRecord));
        }
        return dto;
    }
}
