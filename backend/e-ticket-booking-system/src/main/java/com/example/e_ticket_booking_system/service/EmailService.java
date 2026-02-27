package com.example.e_ticket_booking_system.service;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            String html = new String(
                    new ClassPathResource("templates/otp-email.html").getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8);
            html = html.replace("{{OTP}}", otp);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject("Verify your email - E-Ticket OTP");
            helper.setText(html, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException | java.io.IOException e) {
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }
}
