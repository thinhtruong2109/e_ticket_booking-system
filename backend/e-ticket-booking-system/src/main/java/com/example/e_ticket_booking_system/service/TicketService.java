package com.example.e_ticket_booking_system.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_ticket_booking_system.dto.request.CheckInRequest;
import com.example.e_ticket_booking_system.dto.response.CheckInResponse;
import com.example.e_ticket_booking_system.dto.response.TicketResponse;
import com.example.e_ticket_booking_system.entity.*;
import com.example.e_ticket_booking_system.exception.BadRequestException;
import com.example.e_ticket_booking_system.exception.ResourceNotFoundException;
import com.example.e_ticket_booking_system.repository.*;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;
    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final UserRepository userRepository;

    @Transactional
    public void generateTickets(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!"CONFIRMED".equals(booking.getStatus())) {
            throw new BadRequestException("Booking must be confirmed before generating tickets");
        }

        List<BookingDetail> details = bookingDetailRepository.findByBookingId(bookingId);

        for (BookingDetail detail : details) {
            for (int i = 0; i < detail.getQuantity(); i++) {
                String ticketCode = "TKT" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
                String qrCode = generateQrData(ticketCode, booking);

                Ticket ticket = new Ticket();
                ticket.setTicketCode(ticketCode);
                ticket.setBooking(booking);
                ticket.setTicketType(detail.getTicketType());
                ticket.setQrCode(qrCode);
                ticket.setCurrentOwner(booking.getCustomer());

                // Determine transferability
                Event event = booking.getEvent();
                ticket.setIsTransferable(event.getAllowTicketExchange() != null && event.getAllowTicketExchange());
                ticket.setIsCheckedIn(false);

                ticketRepository.save(ticket);
                log.info("Ticket generated: {} for booking: {}", ticketCode, booking.getBookingCode());
            }
        }
    }

    public List<TicketResponse> getTicketsByBooking(Long bookingId) {
        return ticketRepository.findByBookingId(bookingId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<TicketResponse> getMyTickets(Long userId) {
        return ticketRepository.findByCurrentOwnerId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TicketResponse getTicketByCode(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode);
        if (ticket == null) {
            throw new ResourceNotFoundException("Ticket not found with code: " + ticketCode);
        }
        return toResponse(ticket);
    }

    @Transactional
    public CheckInResponse checkIn(Long staffId, CheckInRequest request) {
        Ticket ticket = ticketRepository.findByTicketCode(request.getTicketCode());
        if (ticket == null) {
            return new CheckInResponse(false, "Ticket not found", request.getTicketCode(), null, null, null);
        }

        if (ticket.getIsCheckedIn() != null && ticket.getIsCheckedIn()) {
            return new CheckInResponse(false,
                    "Ticket already checked in at " + ticket.getCheckedInAt(),
                    ticket.getTicketCode(),
                    ticket.getBooking().getEvent().getName(),
                    null,
                    ticket.getCurrentOwner().getFullName());
        }

        // Validate schedule if provided
        if (request.getScheduleId() != null && ticket.getBooking().getSchedule() != null) {
            if (!ticket.getBooking().getSchedule().getId().equals(request.getScheduleId())) {
                return new CheckInResponse(false, "Ticket is not for this schedule",
                        ticket.getTicketCode(), ticket.getBooking().getEvent().getName(), null, null);
            }
        }

        // Check in
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));

        ticket.setIsCheckedIn(true);
        ticket.setCheckedInAt(LocalDateTime.now());
        ticket.setCheckedInBy(staff);
        ticketRepository.save(ticket);

        log.info("Ticket checked in: {} by staff: {}", ticket.getTicketCode(), staff.getEmail());

        return new CheckInResponse(true, "Check-in successful",
                ticket.getTicketCode(),
                ticket.getBooking().getEvent().getName(),
                null, // Seat info would come from SeatReservation lookup
                ticket.getCurrentOwner().getFullName());
    }

    private String generateQrData(String ticketCode, Booking booking) {
        return String.format("TICKET:%s|EVENT:%d|SCHEDULE:%s|BOOKING:%s",
                ticketCode,
                booking.getEvent().getId(),
                booking.getSchedule() != null ? booking.getSchedule().getId() : "N/A",
                booking.getBookingCode());
    }

    private TicketResponse toResponse(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.setId(ticket.getId());
        response.setTicketCode(ticket.getTicketCode());
        response.setBookingId(ticket.getBooking().getId());
        response.setBookingCode(ticket.getBooking().getBookingCode());
        response.setEventId(ticket.getBooking().getEvent().getId());
        response.setEventName(ticket.getBooking().getEvent().getName());
        response.setScheduleId(ticket.getBooking().getSchedule() != null ? 
                ticket.getBooking().getSchedule().getId() : null);
        response.setTicketTypeName(ticket.getTicketType().getName());
        response.setQrCode(ticket.getQrCode());
        response.setCurrentOwnerId(ticket.getCurrentOwner().getId());
        response.setCurrentOwnerName(ticket.getCurrentOwner().getFullName());
        response.setIsTransferable(ticket.getIsTransferable());
        response.setIsCheckedIn(ticket.getIsCheckedIn());
        response.setCheckedInAt(ticket.getCheckedInAt());
        response.setCheckedInByName(ticket.getCheckedInBy() != null ? 
                ticket.getCheckedInBy().getFullName() : null);
        response.setCreatedAt(ticket.getCreatedAt());
        return response;
    }
}
