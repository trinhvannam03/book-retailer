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

    //clear orders with a timed-out checkout request after 30 minutes;
    @Scheduled(fixedRate = 10000) //fixedDelay
    public void clearPendingOrders() {
        try {
            orderService.clearPendingOrders();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

