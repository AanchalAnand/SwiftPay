package com.swiftpay.ledger_service.kafka.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentFailedEvent {

    private String transactionId;

    private String status;

    private String reason;
}