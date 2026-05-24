package com.swiftpay.ledger_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class LedgerServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(LedgerServiceApplication.class, args);
	}
}