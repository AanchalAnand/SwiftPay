package com.swiftpay.ledger_service.repository;

import com.swiftpay.ledger_service.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository
        extends JpaRepository<Account, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
       SELECT a FROM Account a
       WHERE a.userId = :userId
       """)
    Optional<Account> findByUserIdForUpdate(
            @Param("userId") String userId
    );
}