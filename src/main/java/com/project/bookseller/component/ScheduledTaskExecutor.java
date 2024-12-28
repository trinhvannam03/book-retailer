package com.project.bookseller.component;

import com.project.bookseller.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Component
public class ScheduledTaskExecutor {
    private final OrderService orderService;


    @Scheduled(fixedRate = 1800000) //fixedDelay
    public void clearPendingOrders() {
        try {
            orderService.clearPendingOrders();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Clearing timeout pending orders...");
    }
}

