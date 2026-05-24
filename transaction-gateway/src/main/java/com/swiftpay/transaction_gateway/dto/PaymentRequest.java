package com.swiftpay.transaction_gateway.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(
            value = "0.01",
            inclusive = true,
            message = "Amount must be greater than 0"
    )
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(
            min = 3,
            max = 3,
            message = "Currency must be 3 characters"
    )
    @Pattern(
            regexp = "^[A-Z]{3}$",
            message = "Currency must be in ISO format like INR, USD"
    )
    private String currency;

    @NotBlank(message = "Sender ID is required")
    private String senderId;

    @NotBlank(message = "Receiver ID is required")
    private String receiverId;

    @Size(
            max = 255,
            message = "Idempotency key length exceeded"
    )
    private String idempotencyKey;
}