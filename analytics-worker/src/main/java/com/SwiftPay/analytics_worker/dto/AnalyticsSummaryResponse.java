package com.SwiftPay.analytics_worker.dto;


import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AnalyticsSummaryResponse {

    private Long totalTransactions;

    private BigDecimal totalVolume;

    private Double averageTransactionAmount;
}