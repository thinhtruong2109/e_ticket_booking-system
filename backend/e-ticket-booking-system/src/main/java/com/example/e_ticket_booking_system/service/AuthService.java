package com.example.e_ticket_booking_system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.e_ticket_booking_system.config.JwtUtil;
import com.example.e_ticket_booking_system.dto.request.LoginRequest;
import com.example.e_ticket_booking_system.dto.request.RefreshTokenRequest;
import com.example.e_ticket_booking_system.dto.request.RegisterRequest;
import com.example.e_ticket_booking_system.dto.response.AuthResponse;
import com.example.e_ticket_booking_system.dto.response.UserResponse;
import com.example.e_ticket_booking_system.entity.User;
import com.example.e_ticket_booking_system.exception.BadRequestException;
import com.example.e_ticket_booking_system.exception.UnauthorizedException;
import com.example.e_ticket_booking_system.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        log.info("Registering user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        // Validate password strength
        validatePasswordStrength(request.getPassword());

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole("CUSTOMER");
        user.setStatus("ACTIVE");

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getEmail());

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return new AuthResponse(accessToken, refreshToken, toUserResponse(user));
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if ("BANNED".equals(user.getStatus())) {
            throw new BadRequestException("Your account has been banned");
        }

        if ("INACTIVE".equals(user.getStatus())) {
            throw new BadRequestException("Your account is inactive. Please verify your email");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        log.info("User logged in successfully: {}", user.getEmail());
        return new AuthResponse(accessToken, refreshToken, toUserResponse(user));
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refreshing token");

        if (!jwtUtil.validateToken(request.getRefreshToken())) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String email = jwtUtil.getEmailFromToken(request.getRefreshToken());
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UnauthorizedException("User not found");
        }

        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        return new AuthResponse(newAccessToken, request.getRefreshToken(), toUserResponse(user));
    }

    private void validatePasswordStrength(String password) {
        if (password.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new BadRequestException("Password must contain at least one uppercase letter");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new BadRequestException("Password must contain at least one lowercase letter");
        }
        if (!password.matches(".*\\d.*")) {
            throw new BadRequestException("Password must contain at least one digit");
        }
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(), user.getEmail(), user.getFullName(),
                user.getPhoneNumber(), user.getRole(), user.getStatus(),
                user.getCreatedAt());
    }
}
