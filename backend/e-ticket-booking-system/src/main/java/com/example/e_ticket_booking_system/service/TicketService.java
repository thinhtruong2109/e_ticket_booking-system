package com.example.e_ticket_booking_system.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_ticket_booking_system.dto.request.CheckInRequest;
import com.example.e_ticket_booking_system.dto.response.CheckInResponse;
import com.example.e_ticket_booking_system.dto.response.TicketResponse;
import com.example.e_ticket_booking_system.entity.Booking;
import com.example.e_ticket_booking_system.entity.BookingDetail;
import com.example.e_ticket_booking_system.entity.Event;
import com.example.e_ticket_booking_system.entity.Ticket;
import com.example.e_ticket_booking_system.entity.User;
import com.example.e_ticket_booking_system.exception.BadRequestException;
import com.example.e_ticket_booking_system.exception.ResourceNotFoundException;
import com.example.e_ticket_booking_system.repository.BookingDetailRepository;
import com.example.e_ticket_booking_system.repository.BookingRepository;
import com.example.e_ticket_booking_system.repository.TicketRepository;
import com.example.e_ticket_booking_system.repository.UserRepository;

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
        // Tìm booking theo ID
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (!optionalBooking.isPresent()) {
            throw new ResourceNotFoundException("Booking not found");
        }
        Booking booking = optionalBooking.get();

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

                // Xác định ticket có chuyển nhượng được không
                Event event = booking.getEvent();
                boolean isTransferable = false;
                if (event.getAllowTicketExchange() != null && event.getAllowTicketExchange()) {
                    isTransferable = true;
                }
                ticket.setIsTransferable(isTransferable);
                ticket.setIsCheckedIn(false);

                ticketRepository.save(ticket);
                log.info("Ticket generated: {} for booking: {}", ticketCode, booking.getBookingCode());
            }
        }
    }

    public List<TicketResponse> getTicketsByBooking(Long bookingId) {
        List<Ticket> tickets = ticketRepository.findByBookingId(bookingId);
        List<TicketResponse> responseList = new ArrayList<>();
        for (Ticket ticket : tickets) {
            TicketResponse response = toResponse(ticket);
            responseList.add(response);
        }
        return responseList;
    }

    public List<TicketResponse> getMyTickets(Long userId) {
        List<Ticket> tickets = ticketRepository.findByCurrentOwnerId(userId);
        List<TicketResponse> responseList = new ArrayList<>();
        for (Ticket ticket : tickets) {
            TicketResponse response = toResponse(ticket);
            responseList.add(response);
        }
        return responseList;
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
        Optional<User> optionalStaff = userRepository.findById(staffId);
        if (!optionalStaff.isPresent()) {
            throw new ResourceNotFoundException("Staff not found");
        }
        User staff = optionalStaff.get();

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

        // Lấy scheduleId nếu có
        if (ticket.getBooking().getSchedule() != null) {
            response.setScheduleId(ticket.getBooking().getSchedule().getId());
        } else {
            response.setScheduleId(null);
        }

        response.setTicketTypeName(ticket.getTicketType().getName());
        response.setQrCode(ticket.getQrCode());
        response.setCurrentOwnerId(ticket.getCurrentOwner().getId());
        response.setCurrentOwnerName(ticket.getCurrentOwner().getFullName());
        response.setIsTransferable(ticket.getIsTransferable());
        response.setIsCheckedIn(ticket.getIsCheckedIn());
        response.setCheckedInAt(ticket.getCheckedInAt());

        // Lấy tên người check-in nếu có
        if (ticket.getCheckedInBy() != null) {
            response.setCheckedInByName(ticket.getCheckedInBy().getFullName());
        } else {
            response.setCheckedInByName(null);
        }

        response.setCreatedAt(ticket.getCreatedAt());
        return response;
    }
}
