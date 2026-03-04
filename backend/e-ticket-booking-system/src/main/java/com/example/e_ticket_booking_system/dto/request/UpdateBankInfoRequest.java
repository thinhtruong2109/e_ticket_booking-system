package com.example.e_ticket_booking_system.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateBankInfoRequest {
    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "Bank account number is required")
    private String bankAccountNumber;

    @NotBlank(message = "Bank account holder name is required")
    private String bankAccountHolder;
}
