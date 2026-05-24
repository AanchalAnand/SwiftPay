package com.swiftpay.transaction_gateway.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(
                        name = "idx_transaction_id",
                        columnList = "transaction_id"
                ),
                @Index(
                        name = "idx_idempotency_key",
                        columnList = "idempotency_key"
                ),
                @Index(
                        name = "idx_sender_id",
                        columnList = "sender_id"
                ),
                @Index(
                        name = "idx_receiver_id",
                        columnList = "receiver_id"
                ),
                @Index(
                        name = "idx_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "transaction_id",
            nullable = false,
            unique = true,
            updatable = false
    )
    private String transactionId;

    @Column(
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal amount;

    @Column(
            nullable = false,
            length = 3
    )
    private String currency;

    @Column(
            name = "sender_id",
            nullable = false
    )
    private String senderId;

    @Column(
            name = "receiver_id",
            nullable = false
    )
    private String receiverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(
            name = "idempotency_key",
            unique = true
    )
    private String idempotencyKey;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {

        Instant now = Instant.now();

        this.createdAt = now;
        this.updatedAt = now;

        if (this.status == null) {
            this.status = PaymentStatus.PENDING;
        }

        if (this.transactionId == null ||
                this.transactionId.isBlank()) {

            this.transactionId = UUID.randomUUID().toString();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}