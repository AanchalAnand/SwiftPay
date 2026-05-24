package com.swiftpay.ledger_service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionHistoryResponse {

    private String transactionId;

    private String entryType;

    private BigDecimal amount;

    private Instant createdAt;
}