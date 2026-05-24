package com.SwiftPay.analytics_worker.repository;


import com.SwiftPay.analytics_worker.entity.PaymentAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentAnalyticsRepository
        extends JpaRepository<PaymentAnalytics, UUID> {

    // Total transaction volume
    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM PaymentAnalytics p
    """)
    BigDecimal getTotalVolume();

    // Total transactions count
    @Query("""
        SELECT COUNT(p)
        FROM PaymentAnalytics p
    """)
    Long getTotalTransactions();

    // Average transaction amount
    @Query("""
        SELECT COALESCE(AVG(p.amount), 0)
        FROM PaymentAnalytics p
    """)
    Double getAverageTransactionAmount();
}