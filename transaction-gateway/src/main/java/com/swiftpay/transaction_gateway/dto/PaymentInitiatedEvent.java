package com.swiftpay.transaction_gateway.dto;


import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInitiatedEvent {

    private String transactionId;

    private String senderId;

    private String receiverId;

    private BigDecimal amount;

    private String currency;
}