package com.example.e_ticket_booking_system.controller;

import java.net.URI;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_ticket_booking_system.config.SecurityUtils;
import com.example.e_ticket_booking_system.dto.request.CreatePaymentRequest;
import com.example.e_ticket_booking_system.dto.response.ApiResponse;
import com.example.e_ticket_booking_system.dto.response.PaymentResponse;
import com.example.e_ticket_booking_system.service.PayOSService;
import com.example.e_ticket_booking_system.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.payos.model.webhooks.WebhookData;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;
    private final PayOSService payOSService;
    private final SecurityUtils securityUtils;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    /**
     * Tạo thanh toán - nếu paymentMethod = "PAYOS" sẽ tạo link PayOS
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Payment created",
                paymentService.createPayment(userId, request)));
    }

    /**
     * Lấy thông tin thanh toán theo bookingId
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByBooking(
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentByBookingId(bookingId)));
    }

    /**
     * Lấy thông tin thanh toán PayOS theo orderCode (đồng bộ trạng thái từ PayOS)
     */
    @GetMapping("/payos/{orderCode}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayOSPaymentInfo(
            @PathVariable long orderCode) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPayOSPaymentInfo(orderCode)));
    }

    /**
     * Hủy thanh toán PayOS
     */
    @PutMapping("/payos/{orderCode}/cancel")
    public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayOSPayment(
            @PathVariable long orderCode) {
        return ResponseEntity.ok(ApiResponse.success("Payment cancelled",
                paymentService.cancelPayOSPayment(orderCode)));
    }

    /**
     * Webhook từ PayOS - endpoint public (không cần authentication)
     * PayOS gửi POST đến đây khi trạng thái thanh toán thay đổi.
     *
     * Payload mẫu từ PayOS:
     * {
     *   "code": "00",
     *   "desc": "success",
     *   "success": true,
     *   "data": {
     *     "orderCode": 123,
     *     "amount": 3000,
     *     "description": "VQRIO123",
     *     "accountNumber": "12345678",
     *     "reference": "TF230204212323",
     *     "transactionDateTime": "2023-02-04 18:25:00",
     *     "currency": "VND",
     *     "paymentLinkId": "...",
     *     "code": "00",
     *     "desc": "Thành công",
     *     "counterAccountBankId": "",
     *     "counterAccountBankName": "",
     *     "counterAccountName": "",
     *     "counterAccountNumber": "",
     *     "virtualAccountName": "",
     *     "virtualAccountNumber": ""
     *   },
     *   "signature": "..."
     * }
     *
     * code "00" trong data = thanh toán thành công
     */
    @PostMapping("/payos/webhook")
    public ResponseEntity<?> handlePayOSWebhook(@RequestBody Object body) {
        try {
            log.info("Received PayOS webhook");

            // Xác thực webhook (giống mẫu: payOS.webhooks().verify(body))
            WebhookData webhookData = payOSService.verifyWebhookData(body);

            long orderCode = webhookData.getOrderCode();
            String dataCode = webhookData.getCode();       // "00" = thành công
            String dataDesc = webhookData.getDesc();
            Long amount = webhookData.getAmount();
            String reference = webhookData.getReference();

            log.info("PayOS webhook verified - orderCode: {}, dataCode: {}, desc: {}, amount: {}, reference: {}",
                    orderCode, dataCode, dataDesc, amount, reference);

            // code "00" trong data = thanh toán thành công
            PaymentResponse response = paymentService.processPayOSWebhook(orderCode, dataCode);

            if (response == null) {
                log.info("PayOS webhook ignored (test/unknown orderCode: {})", orderCode);
            } else {
                log.info("PayOS webhook processed - orderCode: {}, paymentStatus: {}",
                        orderCode, response.getStatus());
            }
            return ResponseEntity.ok(Map.of("success", true));

        } catch (Exception e) {
            log.error("Error processing PayOS webhook: {}", e.getMessage(), e);
            // Luôn trả về 200 OK để PayOS không retry liên tục
            return ResponseEntity.ok(Map.of("success", true));
        }
    }

    /**
     * Return URL - PayOS redirect browser đến đây khi thanh toán xong.
     * Backend đồng bộ trạng thái từ PayOS, rồi redirect sang frontend.
     * Endpoint public (permitAll) - không cần auth vì là browser redirect từ PayOS.
     */
    @GetMapping("/payos/success")
    public ResponseEntity<?> paymentSuccess(
            @RequestParam long orderCode,
            @RequestParam(required = false) String status) {
        String redirectUrl;
        try {
            // Đồng bộ trạng thái từ PayOS (nếu paid → confirm booking + generate tickets)
            PaymentResponse response = paymentService.getPayOSPaymentInfo(orderCode);
            log.info("PayOS success redirect - orderCode: {}, status: {}", orderCode, response.getStatus());
            redirectUrl = frontendUrl + "/payment/success?orderCode=" + orderCode + "&status=" + response.getStatus();
        } catch (Exception e) {
            log.error("Error processing PayOS success for orderCode {}: {}", orderCode, e.getMessage());
            redirectUrl = frontendUrl + "/payment/success?orderCode=" + orderCode + "&status=PENDING";
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(redirectUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    /**
     * Cancel URL - PayOS redirect browser đến đây khi user bấm Huỷ.
     * Backend xử lý huỷ payment + booking + trả ghế, rồi redirect sang frontend.
     * Endpoint public (permitAll) - không cần auth vì là browser redirect từ PayOS.
     */
    @GetMapping("/payos/cancel")
    public ResponseEntity<?> paymentCancel(@RequestParam long orderCode) {
        String redirectUrl;
        try {
            PaymentResponse response = paymentService.cancelPayOSPayment(orderCode);
            log.info("PayOS cancel redirect - orderCode: {}, paymentStatus: {}", orderCode, response.getStatus());
            redirectUrl = frontendUrl + "/payment/cancel?orderCode=" + orderCode + "&status=" + response.getStatus();
        } catch (Exception e) {
            log.warn("Error processing PayOS cancel for orderCode {}: {}", orderCode, e.getMessage());
            redirectUrl = frontendUrl + "/payment/cancel?orderCode=" + orderCode + "&status=CANCELLED";
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(redirectUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
