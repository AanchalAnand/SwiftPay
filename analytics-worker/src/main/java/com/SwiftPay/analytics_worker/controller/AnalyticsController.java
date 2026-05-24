package com.SwiftPay.analytics_worker.controller;


import com.SwiftPay.analytics_worker.dto.AnalyticsSummaryResponse;
import com.SwiftPay.analytics_worker.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummaryResponse>
    getSummary() {

        return ResponseEntity.ok(
                analyticsService.getSummary()
        );
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {

        return ResponseEntity.ok(
                "Analytics Worker is running"
        );
    }
}