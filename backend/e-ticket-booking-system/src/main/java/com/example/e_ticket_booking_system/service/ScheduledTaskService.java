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
    private final EventService eventService;
    private final TicketExchangeService ticketExchangeService;
    private final SeatService seatService;

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

    // Run every 1 minute - release expired seat reservations (safety net)
    @Scheduled(fixedRate = 60000)
    public void releaseExpiredSeatReservations() {
        log.debug("Running scheduled task: release expired seat reservations");
        try {
            seatService.releaseExpiredReservations();
        } catch (Exception e) {
            log.error("Error releasing seat reservations: {}", e.getMessage());
        }
    }

    // Run every hour - update event and schedule statuses
    @Scheduled(cron = "0 0 * * * *")
    public void updateEventStatuses() {
        log.debug("Running scheduled task: update event statuses");
        try {
            eventService.updateEventStatuses();
        } catch (Exception e) {
            log.error("Error updating event statuses: {}", e.getMessage());
        }
    }

    // Run every hour - expire ticket listings
    @Scheduled(cron = "0 0 * * * *")
    public void expireTicketListings() {
        log.debug("Running scheduled task: expire ticket listings");
        try {
            ticketExchangeService.expireListings();
        } catch (Exception e) {
            log.error("Error expiring ticket listings: {}", e.getMessage());
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
