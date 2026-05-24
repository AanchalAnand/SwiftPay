package com.swiftpay.ledger_service.kafka.producer;

import com.swiftpay.ledger_service.kafka.event.PaymentCompletedEvent;
import com.swiftpay.ledger_service.kafka.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentCompleted(
            PaymentCompletedEvent event) {

        kafkaTemplate.send(
                "payment-completed",
                event.getTransactionId(),
                event
        );
    }

    public void publishPaymentFailed(
            PaymentFailedEvent event) {

        kafkaTemplate.send(
                "payment-failed",
                event.getTransactionId(),
                event
        );
    }
}