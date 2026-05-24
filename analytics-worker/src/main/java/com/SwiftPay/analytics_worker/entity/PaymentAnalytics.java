package com.SwiftPay.analytics_worker.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_analytics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentAnalytics {

    @Id
    @GeneratedValue
    private UUID id;

    private String transactionId;

    private BigDecimal amount;

    private String currency;

    private LocalDateTime processedAt;
}