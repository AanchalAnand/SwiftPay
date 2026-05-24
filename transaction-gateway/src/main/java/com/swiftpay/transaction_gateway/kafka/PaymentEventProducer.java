package com.swiftpay.transaction_gateway.kafka;


import com.swiftpay.transaction_gateway.dto.PaymentInitiatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentInitiated(
            PaymentInitiatedEvent event) {

        kafkaTemplate.send(
                "payment-initiated",
                event.getTransactionId(),
                event
        );
    }
}