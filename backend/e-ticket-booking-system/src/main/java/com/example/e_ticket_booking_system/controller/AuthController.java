package com.example.e_ticket_booking_system.controller;


import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_ticket_booking_system.dto.request.LoginRequest;
import com.example.e_ticket_booking_system.dto.request.RefreshTokenRequest;
import com.example.e_ticket_booking_system.dto.request.RegisterRequest;
import com.example.e_ticket_booking_system.dto.request.VerifyOtpRequest;
import com.example.e_ticket_booking_system.dto.response.ApiResponse;
import com.example.e_ticket_booking_system.dto.response.AuthResponse;
import com.example.e_ticket_booking_system.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Value("${app.cookie-secure:false}")
        private boolean cookieSecure;

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }

    // Helper: set token cookies
    private void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        boolean isSecure = cookieSecure; // Set true nếu production (configurable)
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
            .httpOnly(true)
            .secure(isSecure)
            .path("/")
            .maxAge(Duration.ofHours(1))
            .sameSite(cookieSecure ? "None" : "Lax")
            .build();
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
            .httpOnly(true)
            .secure(isSecure)
            .path("/")
            .maxAge(Duration.ofDays(7))
            .sameSite(cookieSecure ? "None" : "Lax")
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthResponse auth = authService.login(request);
        setTokenCookies(response, auth.getAccessToken(), auth.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Login successful", auth.getUser()));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<?>> refreshToken(@RequestBody(required = false) RefreshTokenRequest request,
                                                       jakarta.servlet.http.HttpServletRequest servletRequest,
                                                       HttpServletResponse response) {
        String refreshToken = null;
        if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
            refreshToken = request.getRefreshToken();
        } else if (servletRequest.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : servletRequest.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        RefreshTokenRequest effectiveRequest = new RefreshTokenRequest();
        effectiveRequest.setRefreshToken(refreshToken);

        AuthResponse auth = authService.refreshToken(effectiveRequest);
        setTokenCookies(response, auth.getAccessToken(), auth.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", auth.getUser()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        boolean isSecure = cookieSecure; // Set true nếu production (configurable)
        ResponseCookie clearAccess = ResponseCookie.from("accessToken", "")
            .httpOnly(true).secure(isSecure).path("/").maxAge(0).build();
        ResponseCookie clearRefresh = ResponseCookie.from("refreshToken", "")
            .httpOnly(true).secure(isSecure).path("/").maxAge(0).build();
        response.addHeader(HttpHeaders.SET_COOKIE, clearAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, clearRefresh.toString());
        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyOtpRequest request) {
        authService.verifyEmail(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(ApiResponse.success("Xác nhận email thành công"));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(@RequestBody Map<String, String> body) {
        authService.resendOtp(body.get("email"));
        return ResponseEntity.ok(ApiResponse.success("Đã gửi lại OTP"));
    }
}
