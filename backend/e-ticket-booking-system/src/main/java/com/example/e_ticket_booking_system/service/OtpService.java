package com.example.e_ticket_booking_system.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_ticket_booking_system.entity.EmailOtp;
import com.example.e_ticket_booking_system.repository.EmailOtpRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OtpService {

    private final EmailOtpRepository otpRepository;
    private final EmailService emailService;

    public void generateAndSendOtp(String email) {
        // Xóa OTP cũ (nếu có)
        otpRepository.deleteByEmail(email);

        // Tạo OTP 6 số
        String otp = String.format("%06d", new Random().nextInt(999999));

        EmailOtp emailOtp = new EmailOtp();
        emailOtp.setEmail(email);
        emailOtp.setOtp(otp);
        emailOtp.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otpRepository.save(emailOtp);

        // Gửi email
        emailService.sendOtpEmail(email, otp);
    }

    public boolean verifyOtp(String email, String otp) {
        Optional<EmailOtp> found = otpRepository.findByEmailAndOtpAndUsedFalse(email, otp);

        if (found.isEmpty()) return false;

        EmailOtp emailOtp = found.get();

        // Kiểm tra hết hạn
        if (emailOtp.getExpiresAt().isBefore(LocalDateTime.now())) return false;

        // Đánh dấu đã dùng
        emailOtp.setUsed(true);
        otpRepository.save(emailOtp);

        return true;
    }
}
