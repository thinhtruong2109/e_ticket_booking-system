package com.example.e_ticket_booking_system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduledTaskService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTaskService.class);

    private final BookingService bookingService;
    private final PromoCodeService promoCodeService;

    // Run every 1 minute - expire pending bookings
    @Scheduled(fixedRate = 60000)
    public void expirePendingBookings() {
        log.debug("Running scheduled task: expire pending bookings");
        try {
            bookingService.expireBookings();
        } catch (Exception e) {
            log.error("Error expiring bookings: {}", e.getMessage());
        }
    }

    // Run daily at midnight - expire promo codes
    @Scheduled(cron = "0 0 0 * * *")
    public void expirePromoCodes() {
        log.debug("Running scheduled task: expire promo codes");
        try {
            promoCodeService.expirePromoCodes();
        } catch (Exception e) {
            log.error("Error expiring promo codes: {}", e.getMessage());
        }
    }
}
