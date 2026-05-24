package com.swiftpay.transaction_gateway.kafka;


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