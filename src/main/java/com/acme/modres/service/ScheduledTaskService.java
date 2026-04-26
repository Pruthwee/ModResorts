package com.acme.modres.service;

import com.acme.modres.mbean.reservation.DateChecker;
import com.acme.modres.mbean.reservation.ReservationCheckerData;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.logging.Logger;

/**
 * Cloud-native scheduled task service using Spring's @Scheduled annotation
 * Replaces java.util.Timer with distributed-friendly scheduling
 * Can be externalized to Cloud Scheduler for production deployments
 */
@Service
public class ScheduledTaskService {

    private static final Logger logger = Logger.getLogger(ScheduledTaskService.class.getName());

    /**
     * Example scheduled task - runs every hour
     * In production, this should be replaced with Cloud Scheduler
     */
    @Scheduled(cron = "0 0 * * * *", zone = "UTC")
    public void performScheduledCheck() {
        // Use UTC for all time-based operations
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        logger.info("Scheduled task executed at: " + now.toString());
        
        // Add your scheduled logic here
        // This replaces the java.util.Timer usage
    }

    /**
     * Execute a date check task asynchronously
     * This replaces the Timer-based approach with Spring's scheduling
     */
    public void executeAsyncDateCheck(ReservationCheckerData data) {
        // Execute in a separate thread using Spring's task executor
        DateChecker checker = new DateChecker(data);
        new Thread(checker).start();
    }
}
