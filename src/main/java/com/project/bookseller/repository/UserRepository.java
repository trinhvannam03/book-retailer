package com.project.bookseller.repository;

import com.project.bookseller.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByEmail(String email);

    Optional<User> findUserByPhone(String phone);

    @Query("SELECT u from User u JOIN FETCH u.cartRecords where u.email = :email")
    Optional<User> findUserByEmailWithCartItems(String email);

    @Query("SELECT u from User u JOIN FETCH u.cartRecords where u.phone = :phone")
    Optional<User> findUserByPhoneWithCartItems(String phone);

    boolean existsByEmail(String email);
}
