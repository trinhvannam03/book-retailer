package com.project.bookseller.repository;

import com.project.bookseller.entity.order.OrderInformation;
import com.project.bookseller.entity.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderInformationRepository extends JpaRepository<OrderInformation, Integer> {
    @Query("SELECT o from OrderInformation o JOIN FETCH o.orderRecords r JOIN FETCH r.book JOIN FETCH o.city c JOIN FETCH c.state s  JOIN  FETCH  s.country where o.user.userId = :userId ORDER BY o.orderInformationId DESC")
    List<OrderInformation> findOrderInformationByUserId(Long userId);

    @Query("SELECT o from OrderInformation o JOIN FETCH o.orderRecords r JOIN FETCH r.book where o.orderInformationId = :orderId")
    Optional<OrderInformation> findOrderInformationByOrderInformationId(Long orderId);


    @Query("SELECT o from OrderInformation o JOIN FETCH o.orderRecords r JOIN FETCH r.stockRecord where o.orderStatus = :status")
    List<OrderInformation> findAllOrderInformationByOrderStatus(OrderStatus status);
}
