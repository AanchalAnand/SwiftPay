package com.swiftpay.ledger_service.repository;


import com.swiftpay.ledger_service.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentTransactionRepository
        extends JpaRepository<PaymentTransaction, UUID> {

    Optional<PaymentTransaction>
    findByTransactionId(String transactionId);
}