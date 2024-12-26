package com.project.bookseller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.bookseller.entity.order.OrderInformation;
import com.project.bookseller.entity.order.OrderRecord;
import com.project.bookseller.entity.order.OrderStatus;
import com.project.bookseller.entity.order.PaymentMethod;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderInformationDTO {
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

    public static OrderInformationDTO convertFromEntity(OrderInformation orderInformation) {
        OrderInformationDTO dto = new OrderInformationDTO();
        dto.setOrderStatus(orderInformation.getOrderStatus());
        dto.setDiscount(orderInformation.getDiscount());
        dto.setOrderStatus(orderInformation.getOrderStatus());
        dto.setCancelledAt(orderInformation.getCancelledAt());
        dto.setCompletedAt(orderInformation.getCompletedAt());
        dto.setCreatedAt(orderInformation.getCreatedAt());
        dto.setTotal(orderInformation.getTotal());
        dto.setOrderInformationId(orderInformation.getOrderInformationId());
        dto.setPaymentMethod(orderInformation.getPaymentMethod());
        UserAddressDTO userAddressDTO = new UserAddressDTO();
        userAddressDTO.setFullAddress(orderInformation.getFullAddress());
        dto.setAddress(userAddressDTO);
        for (OrderRecord orderRecord : orderInformation.getOrderRecords()) {
            dto.getItems().add(OrderRecordDTO.convertFromEntity(orderRecord));
        }
        return dto;
    }
}
