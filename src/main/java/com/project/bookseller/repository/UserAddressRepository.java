package com.project.bookseller.repository;

import com.project.bookseller.entity.user.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Integer> {
    @Query("SELECT u FROM UserAddress u " +
            "JOIN FETCH u.city c " +
            "JOIN FETCH  c.state  s " +
            "JOIN FETCH s.country " +
            "WHERE u.user.userId = :userId " +
            "ORDER BY u.userAddressId DESC")
    List<UserAddress> findUserAddressesByUserId(Long userId);

    Optional<UserAddress> findUserAddressByUser_EmailAndUserAddressId(String email, Long addressId);
}
