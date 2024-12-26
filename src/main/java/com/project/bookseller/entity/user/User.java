package com.project.bookseller.entity.user;

import com.project.bookseller.entity.order.OrderInformation;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "users", schema = "bookchain")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userId;
    private String passwordHash;
    @Column(unique = true)
    private String email;
    @Column(unique = true)
    private String phone;
    private String fullName;
    private String profilePicture;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM(\"PLATINUM\", \"SILVER\", \"DIAMOND\", \"BRONZE\")")
    private UserTier userTier;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM(\"SUSPENDED\", \"RESTRICTED\", \"ACTIVE\", \"DELETED\")")
    private AccountStatus accountStatus;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM(\"USER\"")
    private UserRole roleName;


    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    List<CartRecord> cartRecords = new ArrayList<>();


    @OneToMany(mappedBy = "user")
    List<UserAddress> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    List<OrderInformation> orderInformation = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('MALE','FEMALE','OTHER')")
    private Gender gender;
    @Temporal(TemporalType.DATE) // Maps to SQL DATE (ignores time part)
    private Date dateOfBirth;

    @PrePersist
    protected void onCreate() {
        this.setUserTier(UserTier.BRONZE);
        this.setRoleName(UserRole.USER);
        this.setAccountStatus(AccountStatus.ACTIVE);
    }
}
