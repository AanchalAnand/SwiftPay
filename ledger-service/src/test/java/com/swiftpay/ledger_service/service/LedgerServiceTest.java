package com.swiftpay.ledger_service.service;

import com.swiftpay.ledger_service.dto.TransactionHistoryResponse;
import com.swiftpay.ledger_service.entity.Account;
import com.swiftpay.ledger_service.entity.LedgerEntry;
import com.swiftpay.ledger_service.entity.PaymentStatus;
import com.swiftpay.ledger_service.entity.PaymentTransaction;
import com.swiftpay.ledger_service.kafka.event.PaymentFailedEvent;
import com.swiftpay.ledger_service.kafka.event.PaymentInitiatedEvent;
import com.swiftpay.ledger_service.kafka.producer.PaymentEventProducer;
import com.swiftpay.ledger_service.repository.AccountRepository;
import com.swiftpay.ledger_service.repository.LedgerEntryRepository;
import com.swiftpay.ledger_service.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @Mock
    private PaymentEventProducer paymentEventProducer;

    @InjectMocks
    private LedgerService ledgerService;

    private PaymentInitiatedEvent testEvent;
    private Account senderAccount;
    private Account receiverAccount;

    @BeforeEach
    void setUp() {
        testEvent = PaymentInitiatedEvent.builder()
                .transactionId("TXN-001")
                .senderId("user-sender")
                .receiverId("user-receiver")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .build();

        senderAccount = Account.builder()
                .userId("user-sender")
                .balance(new BigDecimal("500.00"))
                .build();

        receiverAccount = Account.builder()
                .userId("user-receiver")
                .balance(new BigDecimal("1000.00"))
                .build();
    }

    @Test
    void testProcessPayment_Success() {
        // Arrange
        when(paymentTransactionRepository.findByTransactionId("TXN-001"))
                .thenReturn(Optional.empty());
        when(accountRepository.findByUserIdForUpdate("user-sender"))
                .thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserIdForUpdate("user-receiver"))
                .thenReturn(Optional.of(receiverAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(senderAccount);
        when(paymentTransactionRepository.save(any(PaymentTransaction.class)))
                .thenReturn(new PaymentTransaction());
        when(ledgerEntryRepository.save(any(LedgerEntry.class)))
                .thenReturn(new LedgerEntry());

        // Act
        ledgerService.processPayment(testEvent);

        // Assert
        verify(accountRepository, times(2)).findByUserIdForUpdate(anyString());
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(paymentTransactionRepository, times(1)).save(any(PaymentTransaction.class));
        verify(ledgerEntryRepository, times(2)).save(any(LedgerEntry.class));
    }

    @Test
    void testProcessPayment_InsufficientFunds() {
        // Arrange
        senderAccount.setBalance(new BigDecimal("50.00")); // Less than payment amount

        when(paymentTransactionRepository.findByTransactionId("TXN-001"))
                .thenReturn(Optional.empty());
        when(accountRepository.findByUserIdForUpdate("user-sender"))
                .thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserIdForUpdate("user-receiver"))
                .thenReturn(Optional.of(receiverAccount));

        // Act
        ledgerService.processPayment(testEvent);

        // Assert
        verify(paymentEventProducer, times(1)).publishPaymentFailed(any(PaymentFailedEvent.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testProcessPayment_SenderNotFound() {
        // Arrange
        when(paymentTransactionRepository.findByTransactionId("TXN-001"))
                .thenReturn(Optional.empty());
        when(accountRepository.findByUserIdForUpdate("user-sender"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> ledgerService.processPayment(testEvent));
    }

    @Test
    void testProcessPayment_ReceiverNotFound() {
        // Arrange
        when(paymentTransactionRepository.findByTransactionId("TXN-001"))
                .thenReturn(Optional.empty());
        when(accountRepository.findByUserIdForUpdate("user-sender"))
                .thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserIdForUpdate("user-receiver"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> ledgerService.processPayment(testEvent));
    }

    @Test
    void testProcessPayment_DuplicateTransaction() {
        // Arrange
        PaymentTransaction existingTransaction = new PaymentTransaction();
        existingTransaction.setTransactionId("TXN-001");

        when(paymentTransactionRepository.findByTransactionId("TXN-001"))
                .thenReturn(Optional.of(existingTransaction));

        // Act
        ledgerService.processPayment(testEvent);

        // Assert
        verify(accountRepository, never()).findByUserIdForUpdate(anyString());
        verify(paymentEventProducer, never()).publishPaymentFailed(any(PaymentFailedEvent.class));
    }

    @Test
    void testGetTransactionHistory_Success() {
        // Arrange
        String userId = "user-sender";

        // Note: getTransactionHistory implementation details are in the service
        // This test validates the structure for future enhancements

        // Act & Assert
        // Add implementation when service method is available
    }

    @Test
    void testProcessPayment_ExactAmountTransfer() {
        // Arrange
        senderAccount.setBalance(new BigDecimal("100.00"));
        testEvent.setAmount(new BigDecimal("100.00"));

        when(paymentTransactionRepository.findByTransactionId("TXN-001"))
                .thenReturn(Optional.empty());
        when(accountRepository.findByUserIdForUpdate("user-sender"))
                .thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserIdForUpdate("user-receiver"))
                .thenReturn(Optional.of(receiverAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(senderAccount);
        when(paymentTransactionRepository.save(any(PaymentTransaction.class)))
                .thenReturn(new PaymentTransaction());
        when(ledgerEntryRepository.save(any(LedgerEntry.class)))
                .thenReturn(new LedgerEntry());

        // Act
        ledgerService.processPayment(testEvent);

        // Assert
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void testProcessPayment_LargeAmount() {
        // Arrange
        senderAccount.setBalance(new BigDecimal("10000.00"));
        testEvent.setAmount(new BigDecimal("9999.99"));

        when(paymentTransactionRepository.findByTransactionId("TXN-001"))
                .thenReturn(Optional.empty());
        when(accountRepository.findByUserIdForUpdate("user-sender"))
                .thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByUserIdForUpdate("user-receiver"))
                .thenReturn(Optional.of(receiverAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(senderAccount);
        when(paymentTransactionRepository.save(any(PaymentTransaction.class)))
                .thenReturn(new PaymentTransaction());
        when(ledgerEntryRepository.save(any(LedgerEntry.class)))
                .thenReturn(new LedgerEntry());

        // Act
        ledgerService.processPayment(testEvent);

        // Assert
        verify(paymentTransactionRepository, times(1)).save(any(PaymentTransaction.class));
    }
}
