package com.example.e_ticket_booking_system.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_ticket_booking_system.dto.request.ApplyPromoCodeRequest;
import com.example.e_ticket_booking_system.dto.request.BookingItemRequest;
import com.example.e_ticket_booking_system.dto.request.CreateBookingRequest;
import com.example.e_ticket_booking_system.dto.response.BookingDetailResponse;
import com.example.e_ticket_booking_system.dto.response.BookingResponse;
import com.example.e_ticket_booking_system.entity.*;
import com.example.e_ticket_booking_system.exception.BadRequestException;
import com.example.e_ticket_booking_system.exception.ForbiddenException;
import com.example.e_ticket_booking_system.exception.ResourceNotFoundException;
import com.example.e_ticket_booking_system.repository.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final BookingPromoCodeRepository bookingPromoCodeRepository;
    private final EventRepository eventRepository;
    private final EventScheduleRepository scheduleRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final SeatRepository seatRepository;
    private final SeatReservationRepository seatReservationRepository;
    private final UserRepository userRepository;
    private final PromocodeRepository promoCodeRepository;

    @Value("${booking.hold-duration-minutes:15}")
    private int holdDurationMinutes;

    @Transactional
    public BookingResponse createBooking(Long customerId, CreateBookingRequest request) {
        log.info("Creating booking for customer: {} event: {}", customerId, request.getEventId());

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (!"PUBLISHED".equals(event.getStatus())) {
            throw new BadRequestException("Event is not available for booking");
        }

        EventSchedule schedule = null;
        if (request.getScheduleId() != null) {
            schedule = scheduleRepository.findById(request.getScheduleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Schedule not found"));
            if (!"SCHEDULED".equals(schedule.getStatus())) {
                throw new BadRequestException("Schedule is not available");
            }
        }

        // Calculate total amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<BookingDetail> details = new ArrayList<>();

        for (BookingItemRequest item : request.getItems()) {
            TicketType ticketType = ticketTypeRepository.findById(item.getTicketTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found: " + item.getTicketTypeId()));

            if (!ticketType.getEvent().getId().equals(event.getId())) {
                throw new BadRequestException("Ticket type does not belong to this event");
            }

            if (ticketType.getAvailableQuantity() < item.getQuantity()) {
                throw new BadRequestException("Not enough tickets available for: " + ticketType.getName());
            }

            if (item.getQuantity() > ticketType.getMaxPerBooking()) {
                throw new BadRequestException("Exceeded max per booking for: " + ticketType.getName() + 
                        " (max: " + ticketType.getMaxPerBooking() + ")");
            }

            BigDecimal subtotal = ticketType.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            BookingDetail detail = new BookingDetail();
            detail.setTicketType(ticketType);
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(ticketType.getPrice());
            detail.setSubtotal(subtotal);
            details.add(detail);
        }

        // Handle seat reservations
        if (request.getSeatIds() != null && !request.getSeatIds().isEmpty() && schedule != null) {
            for (Long seatId : request.getSeatIds()) {
                Seat seat = seatRepository.findById(seatId)
                        .orElseThrow(() -> new ResourceNotFoundException("Seat not found: " + seatId));

                // Check seat availability
                List<SeatReservation> existing = seatReservationRepository
                        .findByEventScheduleIdAndStatus(schedule.getId(), "CONFIRMED");
                existing.addAll(seatReservationRepository
                        .findByEventScheduleIdAndStatus(schedule.getId(), "HOLDING"));

                boolean seatTaken = existing.stream()
                        .anyMatch(r -> r.getSeat().getId().equals(seatId));
                if (seatTaken) {
                    throw new BadRequestException("Seat " + seat.getSeatNumber() + " is already reserved");
                }

                SeatReservation reservation = new SeatReservation();
                reservation.setSeat(seat);
                reservation.setEventSchedule(schedule);
                reservation.setUser(customer);
                reservation.setStatus("HOLDING");
                reservation.setHoldExpiresAt(LocalDateTime.now().plusMinutes(holdDurationMinutes));
                seatReservationRepository.save(reservation);
            }
        }

        // Create booking
        String bookingCode = "BK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Booking booking = new Booking();
        booking.setBookingCode(bookingCode);
        booking.setCustomer(customer);
        booking.setEvent(event);
        booking.setSchedule(schedule);
        booking.setTotalAmount(totalAmount);
        booking.setDiscountAmount(BigDecimal.ZERO);
        booking.setFinalAmount(totalAmount);
        booking.setStatus("PENDING");
        booking.setHoldExpiresAt(LocalDateTime.now().plusMinutes(holdDurationMinutes));

        booking = bookingRepository.save(booking);

        // Save booking details & decrease inventory
        for (BookingDetail detail : details) {
            detail.setBooking(booking);
            bookingDetailRepository.save(detail);

            // Decrease inventory
            TicketType tt = detail.getTicketType();
            tt.setAvailableQuantity(tt.getAvailableQuantity() - detail.getQuantity());
            ticketTypeRepository.save(tt);
        }

        // Decrease event available tickets
        int totalBooked = details.stream().mapToInt(BookingDetail::getQuantity).sum();
        event.setAvailableTickets(event.getAvailableTickets() - totalBooked);
        eventRepository.save(event);

        if (schedule != null && schedule.getAvailableSeats() != null) {
            schedule.setAvailableSeats(schedule.getAvailableSeats() - totalBooked);
            scheduleRepository.save(schedule);
        }

        log.info("Booking created: {} for customer: {}", bookingCode, customer.getEmail());
        return toResponse(booking, details);
    }

    @Transactional
    public BookingResponse applyPromoCode(Long customerId, ApplyPromoCodeRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getCustomer().getId().equals(customerId)) {
            throw new ForbiddenException("This booking does not belong to you");
        }

        if (!"PENDING".equals(booking.getStatus())) {
            throw new BadRequestException("Promo code can only be applied to pending bookings");
        }

        PromoCode promo = promoCodeRepository.findByCode(request.getPromoCode());
        if (promo == null) {
            throw new ResourceNotFoundException("Promo code not found");
        }

        if (!"ACTIVE".equals(promo.getStatus())) {
            throw new BadRequestException("Promo code is not active");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(promo.getValidFrom()) || now.isAfter(promo.getValidTo())) {
            throw new BadRequestException("Promo code has expired or is not yet valid");
        }

        if (promo.getUsageLimit() != null && promo.getUsedCount() >= promo.getUsageLimit()) {
            throw new BadRequestException("Promo code usage limit reached");
        }

        if (promo.getMinOrderAmount() != null && 
            booking.getTotalAmount().compareTo(promo.getMinOrderAmount()) < 0) {
            throw new BadRequestException("Order amount does not meet minimum for this promo code");
        }

        // Calculate discount
        BigDecimal discount;
        if ("PERCENTAGE".equals(promo.getDiscountType())) {
            discount = booking.getTotalAmount()
                    .multiply(promo.getDiscountValue())
                    .divide(BigDecimal.valueOf(100));
            if (promo.getMaxDiscountAmount() != null && discount.compareTo(promo.getMaxDiscountAmount()) > 0) {
                discount = promo.getMaxDiscountAmount();
            }
        } else {
            discount = promo.getDiscountValue();
        }

        booking.setDiscountAmount(discount);
        booking.setFinalAmount(booking.getTotalAmount().subtract(discount));
        bookingRepository.save(booking);

        // Record promo usage
        BookingPromoCode bpc = new BookingPromoCode();
        bpc.setBooking(booking);
        bpc.setPromoCode(promo);
        bpc.setDiscountApplied(discount);
        bookingPromoCodeRepository.save(bpc);

        // Increment usage
        promo.setUsedCount(promo.getUsedCount() + 1);
        promoCodeRepository.save(promo);

        log.info("Promo code {} applied to booking {}", promo.getCode(), booking.getBookingCode());

        List<BookingDetail> details = bookingDetailRepository.findByBookingId(booking.getId());
        return toResponse(booking, details);
    }

    @Transactional
    public BookingResponse cancelBooking(Long customerId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getCustomer().getId().equals(customerId)) {
            throw new ForbiddenException("This booking does not belong to you");
        }

        if (!"PENDING".equals(booking.getStatus()) && !"CONFIRMED".equals(booking.getStatus())) {
            throw new BadRequestException("Booking cannot be cancelled in current status: " + booking.getStatus());
        }

        // Release seats
        List<SeatReservation> reservations = seatReservationRepository.findByBookingId(bookingId);
        for (SeatReservation reservation : reservations) {
            reservation.setStatus("RELEASED");
            seatReservationRepository.save(reservation);
        }

        // Restore inventory
        List<BookingDetail> details = bookingDetailRepository.findByBookingId(bookingId);
        restoreInventory(booking, details);

        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
        log.info("Booking cancelled: {}", booking.getBookingCode());

        return toResponse(booking, details);
    }

    @Transactional
    public void expireBookings() {
        List<Booking> expired = bookingRepository
                .findByStatusAndHoldExpiresAtBefore("PENDING", LocalDateTime.now());

        for (Booking booking : expired) {
            List<SeatReservation> reservations = seatReservationRepository.findByBookingId(booking.getId());
            for (SeatReservation reservation : reservations) {
                reservation.setStatus("RELEASED");
                seatReservationRepository.save(reservation);
            }

            List<BookingDetail> details = bookingDetailRepository.findByBookingId(booking.getId());
            restoreInventory(booking, details);

            booking.setStatus("EXPIRED");
            bookingRepository.save(booking);
            log.info("Booking expired: {}", booking.getBookingCode());
        }
    }

    @Transactional
    public void confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        booking.setStatus("CONFIRMED");
        bookingRepository.save(booking);

        // Confirm seat reservations
        List<SeatReservation> reservations = seatReservationRepository.findByBookingId(bookingId);
        for (SeatReservation reservation : reservations) {
            reservation.setStatus("CONFIRMED");
            seatReservationRepository.save(reservation);
        }

        log.info("Booking confirmed: {}", booking.getBookingCode());
    }

    public List<BookingResponse> getMyBookings(Long customerId) {
        List<Booking> bookings = bookingRepository.findByCustomerId(customerId);
        return bookings.stream().map(b -> {
            List<BookingDetail> details = bookingDetailRepository.findByBookingId(b.getId());
            return toResponse(b, details);
        }).collect(Collectors.toList());
    }

    public BookingResponse getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getCustomer().getId().equals(userId)) {
            throw new ForbiddenException("This booking does not belong to you");
        }

        List<BookingDetail> details = bookingDetailRepository.findByBookingId(bookingId);
        return toResponse(booking, details);
    }

    private void restoreInventory(Booking booking, List<BookingDetail> details) {
        int totalRestored = 0;
        for (BookingDetail detail : details) {
            TicketType tt = detail.getTicketType();
            tt.setAvailableQuantity(tt.getAvailableQuantity() + detail.getQuantity());
            ticketTypeRepository.save(tt);
            totalRestored += detail.getQuantity();
        }

        Event event = booking.getEvent();
        event.setAvailableTickets(event.getAvailableTickets() + totalRestored);
        eventRepository.save(event);

        if (booking.getSchedule() != null && booking.getSchedule().getAvailableSeats() != null) {
            EventSchedule schedule = booking.getSchedule();
            schedule.setAvailableSeats(schedule.getAvailableSeats() + totalRestored);
            scheduleRepository.save(schedule);
        }
    }

    private BookingResponse toResponse(Booking booking, List<BookingDetail> details) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setBookingCode(booking.getBookingCode());
        response.setCustomerId(booking.getCustomer().getId());
        response.setCustomerName(booking.getCustomer().getFullName());
        response.setEventId(booking.getEvent().getId());
        response.setEventName(booking.getEvent().getName());
        response.setScheduleId(booking.getSchedule() != null ? booking.getSchedule().getId() : null);
        response.setTotalAmount(booking.getTotalAmount());
        response.setDiscountAmount(booking.getDiscountAmount());
        response.setFinalAmount(booking.getFinalAmount());
        response.setStatus(booking.getStatus());
        response.setHoldExpiresAt(booking.getHoldExpiresAt());
        response.setCreatedAt(booking.getCreatedAt());

        if (details != null) {
            response.setDetails(details.stream().map(d -> new BookingDetailResponse(
                    d.getId(), d.getTicketType().getId(), d.getTicketType().getName(),
                    d.getQuantity(), d.getUnitPrice(), d.getSubtotal()
            )).collect(Collectors.toList()));
        }

        return response;
    }
}
