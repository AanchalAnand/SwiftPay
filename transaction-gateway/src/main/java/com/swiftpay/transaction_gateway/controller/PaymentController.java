package com.swiftpay.transaction_gateway.controller;

import com.swiftpay.transaction_gateway.dto.PaymentRequest;
import com.swiftpay.transaction_gateway.dto.PaymentResponse;
import com.swiftpay.transaction_gateway.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest req) {

        PaymentResponse resp = paymentService.createPayment(req);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resp);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable String transactionId) {

        PaymentResponse resp = paymentService.getPayment(transactionId);

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok(
                "Payment Gateway Service is running");
    }
}