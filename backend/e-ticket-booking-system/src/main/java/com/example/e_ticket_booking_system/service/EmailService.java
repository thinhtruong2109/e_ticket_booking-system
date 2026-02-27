package com.example.e_ticket_booking_system.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject("Xác nhận email - Mã OTP của bạn");
        message.setText("""
            Xin chào!

            Mã OTP xác nhận email của bạn là: %s

            Mã có hiệu lực trong 5 phút. Vui lòng không chia sẻ mã này cho ai.

            Trân trọng,
            E-Ticket Team
            """.formatted(otp));
        mailSender.send(message);
    }
}
