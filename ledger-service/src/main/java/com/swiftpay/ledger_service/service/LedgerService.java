package com.swiftpay.ledger_service.service;


import com.swiftpay.ledger_service.dto.TransactionHistoryResponse;
import com.swiftpay.ledger_service.entity.Account;
import com.swiftpay.ledger_service.entity.LedgerEntry;
import com.swiftpay.ledger_service.entity.PaymentStatus;
import com.swiftpay.ledger_service.entity.PaymentTransaction;
import com.swiftpay.ledger_service.kafka.event.PaymentCompletedEvent;
import com.swiftpay.ledger_service.kafka.event.PaymentFailedEvent;
import com.swiftpay.ledger_service.kafka.event.PaymentInitiatedEvent;
import com.swiftpay.ledger_service.kafka.producer.PaymentEventProducer;
import com.swiftpay.ledger_service.repository.AccountRepository;
import com.swiftpay.ledger_service.repository.LedgerEntryRepository;
import com.swiftpay.ledger_service.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final AccountRepository accountRepository;

    private final LedgerEntryRepository ledgerEntryRepository;

    private final PaymentTransactionRepository
            paymentTransactionRepository;

    private final PaymentEventProducer paymentEventProducer;

    @Transactional
    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public void processPayment(PaymentInitiatedEvent event) {

        if (paymentTransactionRepository.findByTransactionId(event.getTransactionId()).isPresent()) {
            return;
        }

        Account sender = accountRepository.findByUserIdForUpdate(event.getSenderId())
                .orElseThrow(() ->
                        new RuntimeException("Sender not found"));

        Account receiver = accountRepository.findByUserIdForUpdate(event.getReceiverId())
                .orElseThrow(() ->
                        new RuntimeException("Receiver not found"));

        // Insufficient balance check
        if (sender.getBalance()
                .compareTo(event.getAmount()) < 0) {

            paymentEventProducer.publishPaymentFailed(
                    PaymentFailedEvent.builder()
                            .transactionId(event.getTransactionId())
                            .status("FAILED")
                            .reason("INSUFFICIENT_FUNDS")
                            .build()
            );

            return;
        }

        // Debit sender
        sender.setBalance(sender.getBalance().subtract(event.getAmount()));

        // Credit receiver
        receiver.setBalance(receiver.getBalance().add(event.getAmount()));

        accountRepository.save(sender);
        accountRepository.save(receiver);

        // Save transaction
        PaymentTransaction transaction =
                PaymentTransaction.builder()
                        .transactionId(event.getTransactionId())
                        .senderId(event.getSenderId())
                        .receiverId(event.getReceiverId())
                        .amount(event.getAmount())
                        .currency(event.getCurrency())
                        .status(PaymentStatus.COMPLETED)
                        .build();

        paymentTransactionRepository.save(transaction);

        // Debit entry
        ledgerEntryRepository.save(
                LedgerEntry.builder()
                        .transactionId(event.getTransactionId())
                        .userId(event.getSenderId())
                        .amount(event.getAmount())
                        .entryType("DEBIT")
                        .build()
        );

        // Credit entry
        ledgerEntryRepository.save(
                LedgerEntry.builder()
                        .transactionId(event.getTransactionId())
                        .userId(event.getReceiverId())
                        .amount(event.getAmount())
                        .entryType("CREDIT")
                        .build()
        );

        // Publish success event
        paymentEventProducer.publishPaymentCompleted(
                PaymentCompletedEvent.builder()
                        .transactionId(event.getTransactionId())
                        .status("COMPLETED")
                        .amount(event.getAmount())
                        .currency(event.getCurrency())
                        .build()
        );


    }
    @Recover
    public void recover(
            Exception ex,
            PaymentInitiatedEvent event) {

        paymentEventProducer.publishPaymentFailed(
                PaymentFailedEvent.builder()
                        .transactionId(event.getTransactionId())
                        .status("FAILED")
                        .reason(ex.getMessage())
                        .build()
        );
    }

    @Transactional(readOnly = true)
    public List<TransactionHistoryResponse>
    getTransactionHistory(String userId) {

        return ledgerEntryRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(entry ->
                        TransactionHistoryResponse.builder()
                                .transactionId(entry.getTransactionId())
                                .entryType(entry.getEntryType())
                                .amount(entry.getAmount())
                                .createdAt(entry.getCreatedAt())
                                .build()
                )
                .toList();
    }
}