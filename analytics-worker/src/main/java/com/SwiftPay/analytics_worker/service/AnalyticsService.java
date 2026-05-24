package com.SwiftPay.analytics_worker.service;

import com.SwiftPay.analytics_worker.dto.AnalyticsSummaryResponse;
import com.SwiftPay.analytics_worker.entity.PaymentAnalytics;
import com.SwiftPay.analytics_worker.kafka.PaymentCompletedEvent;
import com.SwiftPay.analytics_worker.repository.PaymentAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final PaymentAnalyticsRepository repository;

    @Transactional
    public void saveAnalytics(
            PaymentCompletedEvent event) {

        PaymentAnalytics analytics =
                PaymentAnalytics.builder()
                        .transactionId(event.getTransactionId())
                        .amount(event.getAmount())
                        .currency(event.getCurrency())
                        .processedAt(LocalDateTime.now())
                        .build();

        repository.save(analytics);
    }

    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse getSummary() {

        return AnalyticsSummaryResponse.builder()
                .totalTransactions(
                        repository.getTotalTransactions()
                )
                .totalVolume(
                        repository.getTotalVolume()
                )
                .averageTransactionAmount(
                        repository.getAverageTransactionAmount()
                )
                .build();
    }
}