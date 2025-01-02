package com.project.bookseller.entity.order;

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
    private long orderInformationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;
    private String fullAddress;
    private String phone;
    private String fullName;
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
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;
    private double discount = 0;
    private String coupon;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "orderInformation", fetch = FetchType.LAZY)
    List<OrderRecord> orderRecords = new ArrayList<>();
}
