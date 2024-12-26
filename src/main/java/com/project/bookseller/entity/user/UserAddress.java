package com.project.bookseller.entity.user;

import com.project.bookseller.entity.user.address.City;
import com.project.bookseller.entity.user.address.Coordinates;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class UserAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userAddressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    private String detailedAddress;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city = new City();
    @Embedded
    private Coordinates coordinates;
    private String fullName;
    private String phone;
}
