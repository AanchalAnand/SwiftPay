package com.SwiftPay.analytics_worker.service;

import com.SwiftPay.analytics_worker.dto.AnalyticsSummaryResponse;
import com.SwiftPay.analytics_worker.entity.PaymentAnalytics;
import com.SwiftPay.analytics_worker.kafka.PaymentCompletedEvent;
import com.SwiftPay.analytics_worker.repository.PaymentAnalyticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private PaymentAnalyticsRepository repository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private PaymentCompletedEvent testEvent;

    @BeforeEach
    void setUp() {
        testEvent = PaymentCompletedEvent.builder()
                .transactionId("TXN-001")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .build();
    }

    @Test
    void testSaveAnalytics_Success() {
        // Arrange
        PaymentAnalytics expectedAnalytics = PaymentAnalytics.builder()
                .transactionId(testEvent.getTransactionId())
                .amount(testEvent.getAmount())
                .currency(testEvent.getCurrency())
                .build();

        when(repository.save(any(PaymentAnalytics.class))).thenReturn(expectedAnalytics);

        // Act
        analyticsService.saveAnalytics(testEvent);

        // Assert
        verify(repository, times(1)).save(any(PaymentAnalytics.class));
    }

    @Test
    void testSaveAnalytics_WithNullAmount() {
        // Arrange
        PaymentCompletedEvent eventWithNullAmount = PaymentCompletedEvent.builder()
                .transactionId("TXN-002")
                .amount(null)
                .currency("USD")
                .build();

        when(repository.save(any(PaymentAnalytics.class))).thenReturn(new PaymentAnalytics());

        // Act
        assertDoesNotThrow(() -> analyticsService.saveAnalytics(eventWithNullAmount));

        // Assert
        verify(repository, times(1)).save(any(PaymentAnalytics.class));
    }

    @Test
    void testGetSummary_EmptyData() {
        // Arrange
        when(repository.getTotalTransactions()).thenReturn(0L);
        when(repository.getTotalVolume()).thenReturn(null);
        when(repository.getAverageTransactionAmount()).thenReturn(null);

        // Act
        AnalyticsSummaryResponse result = analyticsService.getSummary();

        // Assert
        assertNotNull(result);
        assertEquals(0L, result.getTotalTransactions());
    }

    @Test
    void testGetSummary_RepositoryException() {
        // Arrange
        when(repository.getTotalTransactions()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> analyticsService.getSummary());
    }

    @Test
    void testSaveAnalytics_DifferentCurrencies() {
        // Arrange
        PaymentCompletedEvent eurEvent = PaymentCompletedEvent.builder()
                .transactionId("TXN-EUR-001")
                .amount(new BigDecimal("85.00"))
                .currency("EUR")
                .build();

        when(repository.save(any(PaymentAnalytics.class))).thenReturn(new PaymentAnalytics());

        // Act
        analyticsService.saveAnalytics(eurEvent);

        // Assert
        verify(repository, times(1)).save(any(PaymentAnalytics.class));
    }

    @Test
    void testSaveAnalytics_LargeAmount() {
        // Arrange
        PaymentCompletedEvent largeEvent = PaymentCompletedEvent.builder()
                .transactionId("TXN-LARGE")
                .amount(new BigDecimal("999999.99"))
                .currency("USD")
                .build();

        when(repository.save(any(PaymentAnalytics.class))).thenReturn(new PaymentAnalytics());

        // Act
        analyticsService.saveAnalytics(largeEvent);

        // Assert
        verify(repository, times(1)).save(any(PaymentAnalytics.class));
    }
}
