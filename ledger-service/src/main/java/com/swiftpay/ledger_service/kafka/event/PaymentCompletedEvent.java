package com.swiftpay.ledger_service.kafka.event;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCompletedEvent {

    private String transactionId;

    private String status;
    private BigDecimal amount;

    private String currency;
}