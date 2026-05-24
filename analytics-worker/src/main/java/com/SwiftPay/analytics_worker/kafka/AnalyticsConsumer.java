package com.SwiftPay.analytics_worker.kafka;

import com.SwiftPay.analytics_worker.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsConsumer {

    private final AnalyticsService analyticsService;

    @KafkaListener(
            topics = "payment-completed",
            groupId = "analytics-group"
    )
    public void consume(
            PaymentCompletedEvent event) {

        log.info(
                "Analytics received transaction: {}",
                event.getTransactionId()
        );

        analyticsService.saveAnalytics(event);
    }
}