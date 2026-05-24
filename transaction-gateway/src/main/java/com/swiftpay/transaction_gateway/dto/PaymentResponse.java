package com.swiftpay.transaction_gateway.dto;

import com.swiftpay.transaction_gateway.model.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private String transactionId;

    private BigDecimal amount;

    private String currency;

    private String senderId;

    private String receiverId;

    private PaymentStatus status;

    private String idempotencyKey;

    private String failureReason;

    private Instant createdAt;

    private Instant updatedAt;
}