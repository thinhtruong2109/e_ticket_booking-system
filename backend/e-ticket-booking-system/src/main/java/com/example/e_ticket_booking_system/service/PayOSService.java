package com.example.e_ticket_booking_system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.WebhookData;

@Service
@RequiredArgsConstructor
public class PayOSService {

    private static final Logger log = LoggerFactory.getLogger(PayOSService.class);

    private final PayOS payOS;

    /**
     * Tạo link thanh toán PayOS (theo SDK v2)
     */
    public CreatePaymentLinkResponse createPaymentLink(
            long orderCode,
            long amount,
            String description,
            String buyerName,
            String buyerEmail,
            String buyerPhone,
            String returnUrl,
            String cancelUrl) throws Exception {

        // Truncate description nếu quá 25 ký tự (giới hạn PayOS)
        if (description != null && description.length() > 25) {
            description = description.substring(0, 25);
        }

        PaymentLinkItem item = PaymentLinkItem.builder()
                .name(description)
                .quantity(1)
                .price(amount)
                .build();

        CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(amount)
                .description(description)
                .buyerName(buyerName)
                .buyerEmail(buyerEmail)
                .buyerPhone(buyerPhone)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .item(item)
                .build();

        log.info("Creating PayOS payment link for orderCode: {}, amount: {}", orderCode, amount);
        CreatePaymentLinkResponse response = payOS.paymentRequests().create(paymentData);
        log.info("PayOS payment link created: {}", response.getCheckoutUrl());
        return response;
    }

    /**
     * Lấy thông tin thanh toán từ PayOS theo orderCode
     */
    public PaymentLink getPaymentInfo(long orderCode) throws Exception {
        return payOS.paymentRequests().get(orderCode);
    }

    /**
     * Hủy link thanh toán PayOS
     */
    public PaymentLink cancelPayment(long orderCode) throws Exception {
        return payOS.paymentRequests().cancel(orderCode, "Hủy theo yêu cầu");
    }

    /**
     * Xác thực dữ liệu webhook từ PayOS (giống mẫu: payOS.webhooks().verify(body))
     *
     * @param body raw request body từ PayOS webhook
     * @return WebhookData nếu hợp lệ
     */
    public WebhookData verifyWebhookData(Object body) throws Exception {
        return payOS.webhooks().verify(body);
    }
}
