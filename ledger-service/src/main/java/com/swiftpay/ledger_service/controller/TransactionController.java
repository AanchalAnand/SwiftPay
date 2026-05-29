package com.swiftpay.ledger_service.controller;

import com.swiftpay.ledger_service.dto.TransactionHistoryResponse;
import com.swiftpay.ledger_service.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final LedgerService ledgerService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<TransactionHistoryResponse>> getTransactions(@PathVariable String userId) {

        return ResponseEntity.ok(
                ledgerService.getTransactionHistory(userId)
        );
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {

        return ResponseEntity.ok(
                "Ledger Service is running"
        );
    }
}