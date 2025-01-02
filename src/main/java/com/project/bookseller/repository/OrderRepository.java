package com.project.bookseller.repository;

import com.project.bookseller.entity.order.Order;
import com.project.bookseller.entity.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    @Query("SELECT o from Order o JOIN FETCH o.orderRecords r JOIN FETCH r.book JOIN FETCH o.city c JOIN FETCH c.state s  JOIN  FETCH  s.country where o.user.userId = :userId ORDER BY o.orderInformationId DESC")
    List<Order> findOrdersByUserId(Long userId);

    @Query("SELECT o from Order o JOIN FETCH o.orderRecords r JOIN FETCH r.book where o.orderInformationId = :orderId AND o.user.userId = :userId")
    Optional<Order> findOrderByOrderInformationId(Long userId, Long orderId);

    @Query("SELECT o from Order o JOIN FETCH o.orderRecords r JOIN FETCH r.stockRecord where o.orderStatus = :status")
    List<Order> findAllOrderByOrderStatus(OrderStatus status);

    @Query("SELECT o from Order o JOIN FETCH o.orderRecords r JOIN FETCH r.stockRecord where o.orderInformationId = :orderId")
    Optional<Order> findOrderWithStockByOrderInformationId(Long userId, Long orderId);
}
