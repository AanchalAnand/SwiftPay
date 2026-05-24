package com.swiftpay.ledger_service.kafka.consumer;


import com.swiftpay.ledger_service.kafka.event.PaymentInitiatedEvent;
import com.swiftpay.ledger_service.service.LedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final LedgerService ledgerService;

    @KafkaListener(
            topics = "payment-initiated",
            groupId = "ledger-group"
    )
    public void consumePayment(
            PaymentInitiatedEvent event) {

        log.info(
                "Received payment initiated event: {}",
                event.getTransactionId()
        );

        ledgerService.processPayment(event);
    }
}