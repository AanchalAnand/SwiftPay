package com.swiftpay.transaction_gateway.service;

import com.swiftpay.transaction_gateway.dto.PaymentInitiatedEvent;
import com.swiftpay.transaction_gateway.dto.PaymentRequest;
import com.swiftpay.transaction_gateway.dto.PaymentResponse;
import com.swiftpay.transaction_gateway.exception.InvalidRequestException;
import com.swiftpay.transaction_gateway.exception.PaymentNotFoundException;
import com.swiftpay.transaction_gateway.kafka.PaymentEventProducer;
import com.swiftpay.transaction_gateway.model.Payment;
import com.swiftpay.transaction_gateway.model.PaymentStatus;
import com.swiftpay.transaction_gateway.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;
    private final StringRedisTemplate redisTemplate;


    @Transactional
    public PaymentResponse createPayment(PaymentRequest req) {

        if (req.getAmount() == null
                || req.getAmount().doubleValue() <= 0) {

            throw new InvalidRequestException(
                    "Amount must be greater than 0"
            );
        }

        // Check duplicate request using idempotency key
        if (req.getIdempotencyKey() != null
                && !req.getIdempotencyKey().isBlank()) {

            String redisKey =
                    "payment:" + req.getIdempotencyKey();

            Boolean isNewRequest = redisTemplate
                    .opsForValue()
                    .setIfAbsent(
                            redisKey,
                            "processed",
                            Duration.ofHours(24)
                    );

            // Duplicate request
            if (Boolean.FALSE.equals(isNewRequest)) {

                return paymentRepository
                        .findByIdempotencyKey(
                                req.getIdempotencyKey()
                        )
                        .map(this::toResponse)
                        .orElseThrow(() ->
                                new InvalidRequestException(
                                        "Duplicate transaction request"
                                ));
            }

        }

        // Normal flow
        Payment savedPayment = createNewPayment(req);

        publishPaymentEvent(savedPayment);

        return toResponse(savedPayment);
    }

    private void publishPaymentEvent(
            Payment payment) {

        paymentEventProducer.publishPaymentInitiated(

                PaymentInitiatedEvent.builder()
                        .transactionId(
                                payment.getTransactionId()
                        )
                        .senderId(
                                payment.getSenderId()
                        )
                        .receiverId(
                                payment.getReceiverId()
                        )
                        .amount(
                                payment.getAmount()
                        )
                        .currency(
                                payment.getCurrency()
                        )
                        .build()
        );
    }

    private Payment createNewPayment(PaymentRequest req) {

        Payment payment = Payment.builder()
                .transactionId(UUID.randomUUID().toString())
                .amount(req.getAmount())
                .currency(req.getCurrency())
                .senderId(req.getSenderId())
                .receiverId(req.getReceiverId())
                .status(PaymentStatus.PENDING)
                .idempotencyKey(req.getIdempotencyKey())
                .build();

        return paymentRepository.save(payment);
    }
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(String transactionId) {

        return paymentRepository
                .findByTransactionId(transactionId)
                .map(this::toResponse)
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Payment not found: " + transactionId
                        ));
    }

    private PaymentResponse toResponse(Payment payment) {

        return PaymentResponse.builder()
                .transactionId(payment.getTransactionId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .senderId(payment.getSenderId())
                .receiverId(payment.getReceiverId())
                .status(payment.getStatus())
                .idempotencyKey(payment.getIdempotencyKey())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
