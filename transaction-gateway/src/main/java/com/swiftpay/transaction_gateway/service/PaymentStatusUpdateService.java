package com.swiftpay.transaction_gateway.service;


import com.swiftpay.transaction_gateway.exception.PaymentNotFoundException;
import com.swiftpay.transaction_gateway.model.Payment;
import com.swiftpay.transaction_gateway.model.PaymentStatus;
import com.swiftpay.transaction_gateway.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentStatusUpdateService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public void markPaymentCompleted(
            String transactionId) {

        Payment payment = paymentRepository
                .findByTransactionId(transactionId)
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Payment not found"
                        ));

        payment.setStatus(PaymentStatus.COMPLETED);

        paymentRepository.save(payment);
    }

    @Transactional
    public void markPaymentFailed(
            String transactionId,
            String reason) {

        Payment payment = paymentRepository
                .findByTransactionId(transactionId)
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Payment not found"
                        ));

        payment.setStatus(PaymentStatus.FAILED);

        payment.setFailureReason(reason);

        paymentRepository.save(payment);
    }
}