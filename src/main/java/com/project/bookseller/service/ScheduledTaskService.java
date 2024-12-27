package com.project.bookseller.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduledTaskService {
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


    // Run using a cron expression (e.g., every minute at second 0)
    @Scheduled(cron = "0 * * * * *")
    public void runTaskWithCron() {
        System.out.println("Task running with cron: " + System.currentTimeMillis());
    }
}

