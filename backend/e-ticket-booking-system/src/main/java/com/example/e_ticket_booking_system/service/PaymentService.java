package com.example.e_ticket_booking_system.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_ticket_booking_system.dto.request.CreatePaymentRequest;
import com.example.e_ticket_booking_system.dto.response.PaymentResponse;
import com.example.e_ticket_booking_system.entity.Booking;
import com.example.e_ticket_booking_system.entity.BookingPromoCode;
import com.example.e_ticket_booking_system.entity.Payment;
import com.example.e_ticket_booking_system.entity.PromoCode;
import com.example.e_ticket_booking_system.exception.BadRequestException;
import com.example.e_ticket_booking_system.exception.ForbiddenException;
import com.example.e_ticket_booking_system.exception.ResourceNotFoundException;
import com.example.e_ticket_booking_system.repository.BookingPromoCodeRepository;
import com.example.e_ticket_booking_system.repository.BookingRepository;
import com.example.e_ticket_booking_system.repository.PaymentRepository;
import com.example.e_ticket_booking_system.repository.PromocodeRepository;

import lombok.RequiredArgsConstructor;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkStatus;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BookingPromoCodeRepository bookingPromoCodeRepository;
    private final PromocodeRepository promoCodeRepository;
    private final BookingService bookingService;
    private final TicketService ticketService;
    private final PayOSService payOSService;
    private final TransactionHistoryService transactionHistoryService;
    private final OrganizerEWalletService organizerEWalletService;

    @Value("${payos.return-url}")
    private String returnUrl;

    @Value("${payos.cancel-url}")
    private String cancelUrl;

    @Transactional
    public PaymentResponse createPayment(Long userId, CreatePaymentRequest request) {
        // Tìm booking theo ID
        Booking booking = bookingRepository.findById(request.getBookingId()).orElse(null);
        if (booking == null) {
            throw new ResourceNotFoundException("Booking not found");
        }

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

        // Tạo orderCode cho PayOS (dùng timestamp + bookingId để đảm bảo unique)
        long payosOrderCode = Long.parseLong(
                String.valueOf(System.currentTimeMillis() % 1000000000)
                        + String.valueOf(booking.getId() % 1000));

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setAmount(booking.getFinalAmount());
        payment.setStatus("PENDING");
        payment.setPayosOrderCode(payosOrderCode);

        // Nếu phương thức thanh toán là PAYOS, tạo link thanh toán qua PayOS
        if ("PAYOS".equalsIgnoreCase(request.getPaymentMethod())) {
            try {
                long amountLong = booking.getFinalAmount().longValue();
                String description = "DH" + booking.getBookingCode();

                // Thêm orderCode vào returnUrl và cancelUrl để frontend xử lý
                String fullReturnUrl = returnUrl + "?orderCode=" + payosOrderCode;
                String fullCancelUrl = cancelUrl + "?orderCode=" + payosOrderCode;

                CreatePaymentLinkResponse checkoutData = payOSService.createPaymentLink(
                        payosOrderCode,
                        amountLong,
                        description,
                        booking.getCustomer().getFullName(),
                        booking.getCustomer().getEmail(),
                        booking.getCustomer().getPhoneNumber() != null ? booking.getCustomer().getPhoneNumber() : "",
                        fullReturnUrl,
                        fullCancelUrl);

                payment.setCheckoutUrl(checkoutData.getCheckoutUrl());
                payment.setPaymentLinkId(checkoutData.getPaymentLinkId());

                log.info("PayOS payment link created for booking {}: {}", booking.getBookingCode(),
                        checkoutData.getCheckoutUrl());
            } catch (Exception e) {
                log.error("Failed to create PayOS payment link for booking {}: {}",
                        booking.getBookingCode(), e.getMessage());
                throw new BadRequestException("Failed to create payment link: " + e.getMessage());
            }
        }

        payment = paymentRepository.save(payment);
        log.info("Payment created with orderCode {} for booking: {}", payosOrderCode, booking.getBookingCode());

        // Ghi log lịch sử giao dịch
        transactionHistoryService.logPaymentCreated(payment);

        return toResponse(payment);
    }



    /**
     * Xử lý kết quả thanh toán từ PayOS webhook.
     *
     * @param orderCode PayOS orderCode
     * @param dataCode  Mã kết quả từ webhook data.code: "00" = thành công, khác = thất bại
     */
    @Transactional
    public PaymentResponse processPayOSWebhook(long orderCode, String dataCode) {
        Payment payment = paymentRepository.findByPayosOrderCode(orderCode);
        if (payment == null) {
            // PayOS gửi webhook test với orderCode=123 khi đăng ký webhook URL → bỏ qua
            log.info("No payment found for PayOS orderCode: {}. Likely a test webhook, ignoring.", orderCode);
            return null;
        }

        // Nếu payment đã SUCCESS rồi thì không xử lý lại
        if ("SUCCESS".equals(payment.getStatus())) {
            log.info("Payment already processed for orderCode: {}", orderCode);
            return toResponse(payment);
        }

        // PayOS: data.code = "00" nghĩa là thanh toán thành công
        boolean success = "00".equals(dataCode);
        return processPaymentResult(payment, success);
    }

    /**
     * Xử lý chung cho kết quả thanh toán
     */
    private PaymentResponse processPaymentResult(Payment payment, boolean success) {
        if (success) {
            // Guard: kiểm tra booking vẫn còn PENDING trước khi confirm
            // Phòng race condition: booking đã EXPIRED nhưng webhook vẫn đến
            Booking booking = payment.getBooking();
            if (!"PENDING".equals(booking.getStatus())) {
                log.warn("Payment SUCCESS but booking {} is in status '{}' (not PENDING). "
                        + "Skipping confirmation. OrderCode: {}. Cần kiểm tra và hoàn tiền nếu cần.",
                        booking.getBookingCode(), booking.getStatus(), payment.getPayosOrderCode());
                payment.setStatus("REFUND_REQUIRED");
                paymentRepository.save(payment);
                transactionHistoryService.logPaymentFailed(payment);
                return toResponse(payment);
            }

            payment.setStatus("SUCCESS");
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // Confirm booking and generate tickets
            bookingService.confirmBooking(payment.getBooking().getId());
            ticketService.generateTickets(payment.getBooking().getId());

            // Increment promo code usedCount if a promo was applied
            java.util.List<BookingPromoCode> bpcList = bookingPromoCodeRepository
                    .findByBookingId(payment.getBooking().getId());
            for (BookingPromoCode bpc : bpcList) {
                PromoCode promo = bpc.getPromoCode();
                promo.setUsedCount(promo.getUsedCount() + 1);
                promoCodeRepository.save(promo);
                log.info("Promo code {} usedCount incremented for booking {}",
                        promo.getCode(), payment.getBooking().getBookingCode());
            }

            log.info("Payment successful for orderCode: {}", payment.getPayosOrderCode());

            // Ghi log lịch sử giao dịch thành công
            transactionHistoryService.logPaymentSuccess(payment);

            // Cộng doanh thu vào ví organizer
            try {
                Long organizerId = booking.getEvent().getOrganizer().getId();
                organizerEWalletService.creditRevenue(organizerId, booking.getFinalAmount(), booking.getBookingCode());
            } catch (Exception e) {
                log.error("Failed to credit revenue to organizer wallet for booking {}: {}",
                        booking.getBookingCode(), e.getMessage(), e);
            }
        } else {
            payment.setStatus("FAILED");
            paymentRepository.save(payment);
            log.info("Payment failed for orderCode: {}", payment.getPayosOrderCode());

            // Ghi log lịch sử giao dịch thất bại
            transactionHistoryService.logPaymentFailed(payment);

            // Huỷ booking + trả ghế + trả inventory ngay khi payment thất bại
            // (không cần đợi scheduled task expire)
            bookingService.cancelBookingBySystem(payment.getBooking().getId());
        }

        return toResponse(payment);
    }

    /**
     * Lấy thông tin thanh toán từ PayOS
     */
    public PaymentResponse getPayOSPaymentInfo(long orderCode) {
        Payment payment = paymentRepository.findByPayosOrderCode(orderCode);
        if (payment == null) {
            throw new ResourceNotFoundException("Payment not found with PayOS orderCode: " + orderCode);
        }

        // Cập nhật trạng thái từ PayOS
        try {
            PaymentLink payosData = payOSService.getPaymentInfo(orderCode);
            PaymentLinkStatus payosStatus = payosData.getStatus();

            if (PaymentLinkStatus.PAID.equals(payosStatus) && !"SUCCESS".equals(payment.getStatus())) {
                return processPaymentResult(payment, true);
            } else if (PaymentLinkStatus.CANCELLED.equals(payosStatus) && "PENDING".equals(payment.getStatus())) {
                payment.setStatus("CANCELLED");
                paymentRepository.save(payment);
                transactionHistoryService.logPaymentCancelled(payment);

                // Huỷ booking + trả ghế + trả inventory
                bookingService.cancelBookingBySystem(payment.getBooking().getId());
            }
        } catch (Exception e) {
            log.warn("Could not fetch PayOS payment info for orderCode {}: {}", orderCode, e.getMessage());
        }

        return toResponse(payment);
    }

    /**
     * Hủy thanh toán PayOS (khi user bấm Huỷ trên trang PayOS hoặc gọi API cancel)
     * Đồng thời huỷ booking và trả ghế + inventory.
     */
    @Transactional
    public PaymentResponse cancelPayOSPayment(long orderCode) {
        Payment payment = paymentRepository.findByPayosOrderCode(orderCode);
        if (payment == null) {
            throw new ResourceNotFoundException("Payment not found with PayOS orderCode: " + orderCode);
        }

        // Nếu payment đã bị huỷ rồi thì trả về luôn (idempotent)
        if ("CANCELLED".equals(payment.getStatus())) {
            log.info("Payment already cancelled for orderCode: {}", orderCode);
            return toResponse(payment);
        }

        if (!"PENDING".equals(payment.getStatus())) {
            throw new BadRequestException("Only pending payments can be cancelled");
        }

        // 1. Huỷ payment link trên PayOS (có thể đã bị huỷ rồi, nên bắt lỗi)
        try {
            payOSService.cancelPayment(orderCode);
            log.info("PayOS payment link cancelled for orderCode: {}", orderCode);
        } catch (Exception e) {
            log.warn("Could not cancel PayOS payment link for orderCode {} (may already be cancelled): {}",
                    orderCode, e.getMessage());
        }

        // 2. Cập nhật payment status
        payment.setStatus("CANCELLED");
        paymentRepository.save(payment);
        transactionHistoryService.logPaymentCancelled(payment);

        // 3. Huỷ booking + trả ghế + trả inventory
        bookingService.cancelBookingBySystem(payment.getBooking().getId());

        log.info("PayOS payment and booking cancelled for orderCode: {}", orderCode);
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

        // Ghi log lịch sử hoàn tiền
        transactionHistoryService.logRefund(payment);

        // Trừ tiền hoàn vé khỏi ví organizer
        try {
            Booking booking = payment.getBooking();
            Long organizerId = booking.getEvent().getOrganizer().getId();
            organizerEWalletService.deductRefund(organizerId, payment.getAmount(), booking.getBookingCode());
        } catch (Exception e) {
            log.error("Failed to deduct refund from organizer wallet for booking {}: {}",
                    bookingId, e.getMessage(), e);
        }

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
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setBookingId(payment.getBooking().getId());
        response.setBookingCode(payment.getBooking().getBookingCode());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setAmount(payment.getAmount());
        response.setStatus(payment.getStatus());
        response.setPayosOrderCode(payment.getPayosOrderCode());
        response.setCheckoutUrl(payment.getCheckoutUrl());
        response.setPaymentLinkId(payment.getPaymentLinkId());
        response.setPaidAt(payment.getPaidAt());
        response.setCreatedAt(payment.getCreatedAt());
        return response;
    }
}
