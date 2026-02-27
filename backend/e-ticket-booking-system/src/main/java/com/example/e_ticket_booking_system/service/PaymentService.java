package com.example.e_ticket_booking_system.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_ticket_booking_system.dto.request.CreatePaymentRequest;
import com.example.e_ticket_booking_system.dto.response.PaymentResponse;
import com.example.e_ticket_booking_system.entity.Booking;
import com.example.e_ticket_booking_system.entity.Payment;
import com.example.e_ticket_booking_system.exception.BadRequestException;
import com.example.e_ticket_booking_system.exception.ForbiddenException;
import com.example.e_ticket_booking_system.exception.ResourceNotFoundException;
import com.example.e_ticket_booking_system.repository.BookingRepository;
import com.example.e_ticket_booking_system.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final TicketService ticketService;

    @Transactional
    public PaymentResponse createPayment(Long userId, CreatePaymentRequest request) {
        // Tìm booking theo ID
        Optional<Booking> optionalBooking = bookingRepository.findById(request.getBookingId());
        if (!optionalBooking.isPresent()) {
            throw new ResourceNotFoundException("Booking not found");
        }
        Booking booking = optionalBooking.get();

        if (!booking.getCustomer().getId().equals(userId)) {
            throw new ForbiddenException("This booking does not belong to you");
        }

        if (!"PENDING".equals(booking.getStatus())) {
            throw new BadRequestException("Booking is not in PENDING status");
        }

        // Check if payment already exists
        Payment existingPayment = paymentRepository.findByBookingId(booking.getId());
        if (existingPayment != null && "SUCCESS".equals(existingPayment.getStatus())) {
            throw new BadRequestException("Payment already completed for this booking");
        }

        String transactionId = "TXN" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setAmount(booking.getFinalAmount());
        payment.setStatus("PENDING");
        payment.setTransactionId(transactionId);

        payment = paymentRepository.save(payment);
        log.info("Payment created: {} for booking: {}", transactionId, booking.getBookingCode());
        return toResponse(payment);
    }

    @Transactional
    public PaymentResponse processPaymentCallback(String transactionId, boolean success) {
        Payment payment = paymentRepository.findByTransactionId(transactionId);
        if (payment == null) {
            throw new ResourceNotFoundException("Payment not found with transactionId: " + transactionId);
        }

        if (success) {
            payment.setStatus("SUCCESS");
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // Confirm booking and generate tickets
            bookingService.confirmBooking(payment.getBooking().getId());
            ticketService.generateTickets(payment.getBooking().getId());

            log.info("Payment successful: {}", transactionId);
        } else {
            payment.setStatus("FAILED");
            paymentRepository.save(payment);
            log.info("Payment failed: {}", transactionId);
        }

        return toResponse(payment);
    }

    @Transactional
    public PaymentResponse refundPayment(Long bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId);
        if (payment == null) {
            throw new ResourceNotFoundException("Payment not found for booking: " + bookingId);
        }

        if (!"SUCCESS".equals(payment.getStatus())) {
            throw new BadRequestException("Only successful payments can be refunded");
        }

        payment.setStatus("REFUNDED");
        paymentRepository.save(payment);
        log.info("Payment refunded for booking: {}", bookingId);
        return toResponse(payment);
    }

    public PaymentResponse getPaymentByBookingId(Long bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId);
        if (payment == null) {
            throw new ResourceNotFoundException("Payment not found for booking: " + bookingId);
        }
        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(), payment.getBooking().getId(),
                payment.getBooking().getBookingCode(),
                payment.getPaymentMethod(), payment.getAmount(),
                payment.getStatus(), payment.getTransactionId(),
                payment.getPaidAt(), payment.getCreatedAt());
    }
}
