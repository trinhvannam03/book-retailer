package com.project.bookseller.entity.order;

import com.project.bookseller.dto.order.OrderDTO;
import com.project.bookseller.entity.user.address.City;
import com.project.bookseller.entity.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "order_information")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderInformationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = true, insertable = false, updatable = false)
    private City city;
    @Column(name = "city_id")
    private Long cityId;

    private String fullAddress;
    private String phone;
    private String fullName;
    private String longitude;
    private String latitude;
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime completedAt;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('ONLINE','PICKUP')")
    private OrderType orderType;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('CANCELLED','PROCESSING','COMPLETED','PENDING')")
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('COD','PREPAID')")
    private PaymentMethod paymentMethod;
    private double total = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    @Column(name = "user_id")
    private Long userId;

    private double discount = 0;
    private String appliedCoupon;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    List<OrderRecord> orderRecords = new ArrayList<>();

    public static Order convertFromDTO(OrderDTO dto) {
        Order order = new Order();
        order.setAppliedCoupon(dto.getAppliedCoupon());
        order.setCreatedAt(dto.getCreatedAt());
        order.setCancelledAt(dto.getCancelledAt());
        order.setCompletedAt(dto.getCompletedAt());
        order.setDiscount(dto.getDiscount());
        order.setFullAddress(dto.getFullAddress());
        order.setFullName(dto.getFullName());
        order.setLongitude(dto.getLongitude());
        order.setLatitude(dto.getLatitude());
        order.setOrderStatus(dto.getOrderStatus());
        order.setPhone(dto.getPhone());
        order.setTotal(dto.getTotal());
        order.setOrderType(dto.getOrderType());
        order.setPaymentMethod(dto.getPaymentMethod());
        return order;
    }
}
