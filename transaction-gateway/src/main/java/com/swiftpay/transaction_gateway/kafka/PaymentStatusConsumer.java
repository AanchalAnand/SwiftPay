package com.swiftpay.transaction_gateway.kafka;


import com.swiftpay.transaction_gateway.service.PaymentStatusUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentStatusConsumer {

    private final PaymentStatusUpdateService paymentStatusUpdateService;

    @KafkaListener(
            topics = "payment-completed",
            groupId = "payment-group"
    )
    public void consumeCompleted(PaymentCompletedEvent event) {

        log.info(
                "Payment completed received: {}",
                event.getTransactionId()
        );

        paymentStatusUpdateService
                .markPaymentCompleted(
                        event.getTransactionId()
                );
    }

    @KafkaListener(
            topics = "payment-failed",
            groupId = "payment-group"
    )
    public void consumeFailed(PaymentFailedEvent event) {

        log.info(
                "Payment failed received: {}",
                event.getTransactionId()
        );

        paymentStatusUpdateService
                .markPaymentFailed(
                        event.getTransactionId(),
                        event.getReason()
                );
    }
}