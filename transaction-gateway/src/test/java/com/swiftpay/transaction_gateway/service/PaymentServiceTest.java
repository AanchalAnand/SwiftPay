package com.swiftpay.transaction_gateway.service;

import com.swiftpay.transaction_gateway.dto.PaymentRequest;
import com.swiftpay.transaction_gateway.dto.PaymentResponse;
import com.swiftpay.transaction_gateway.exception.InvalidRequestException;
import com.swiftpay.transaction_gateway.kafka.PaymentEventProducer;
import com.swiftpay.transaction_gateway.model.Payment;
import com.swiftpay.transaction_gateway.model.PaymentStatus;
import com.swiftpay.transaction_gateway.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventProducer paymentEventProducer;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        paymentRequest = PaymentRequest.builder()
                .senderId("user-sender")
                .receiverId("user-receiver")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .idempotencyKey("idempotency-key-123")
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testCreatePayment_Success() {
        // Arrange
        Payment payment = Payment.builder()
                .transactionId("TXN-001")
                .senderId("user-sender")
                .receiverId("user-receiver")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .status(PaymentStatus.PENDING)
                .idempotencyKey("idempotency-key-123")
                .build();

        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // Act
        PaymentResponse response = paymentService.createPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertEquals("TXN-001", response.getTransactionId());
        assertEquals(new BigDecimal("100.00"), response.getAmount());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(paymentEventProducer, times(1)).publishPaymentInitiated(any());
    }



    @Test
    void testCreatePayment_DuplicateRequest() {
        // Arrange
        Payment existingPayment = Payment.builder()
                .transactionId("TXN-001")
                .senderId("user-sender")
                .receiverId("user-receiver")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .status(PaymentStatus.PENDING)
                .idempotencyKey("idempotency-key-123")
                .build();

        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(false);
        when(paymentRepository.findByIdempotencyKey("idempotency-key-123"))
                .thenReturn(Optional.of(existingPayment));

        // Act
        PaymentResponse response = paymentService.createPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertEquals("TXN-001", response.getTransactionId());
        verify(paymentRepository, times(0)).save(any(Payment.class));
    }



    @Test
    void testCreatePayment_DifferentCurrencies() {
        // Arrange
        paymentRequest.setCurrency("EUR");
        Payment payment = Payment.builder()
                .transactionId("TXN-EUR-001")
                .senderId("user-sender")
                .receiverId("user-receiver")
                .amount(new BigDecimal("85.00"))
                .currency("EUR")
                .status(PaymentStatus.PENDING)
                .idempotencyKey("idempotency-key-123")
                .build();

        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // Act
        PaymentResponse response = paymentService.createPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertEquals("EUR", response.getCurrency());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testCreatePayment_LargeAmount() {
        // Arrange
        paymentRequest.setAmount(new BigDecimal("999999.99"));
        Payment payment = Payment.builder()
                .transactionId("TXN-LARGE")
                .senderId("user-sender")
                .receiverId("user-receiver")
                .amount(new BigDecimal("999999.99"))
                .currency("USD")
                .status(PaymentStatus.PENDING)
                .idempotencyKey("idempotency-key-123")
                .build();

        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // Act
        PaymentResponse response = paymentService.createPayment(paymentRequest);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("999999.99"), response.getAmount());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

}
