package com.swiftpay.transaction_gateway.kafka;


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