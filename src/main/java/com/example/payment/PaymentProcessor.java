package com.example.payment;

import com.example.NotificationException;

import java.math.BigDecimal;

public class PaymentProcessor {
    private final PaymentConfig paymentConfig;
    private final PaymentRepository paymentRepository;
    private final PaymentApi paymentApi;
    private final EmailService emailService;

    public PaymentProcessor(
            PaymentConfig paymentConfig,
            PaymentRepository paymentRepository,
            PaymentApi paymentApi,
            EmailService emailService) {
        this.paymentConfig = paymentConfig;
        this.paymentRepository = paymentRepository;
        this.paymentApi = paymentApi;
        this.emailService = emailService;
    }

    public boolean processPayment(String email, BigDecimal amount) throws PaymentException {
        if (email == null || amount == null) {
            throw new IllegalArgumentException("Email and amount cannot be null");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0)  {
            throw new IllegalArgumentException("Amount must be positive");
        }

        // Anropar extern betaltjänst
        PaymentApiResponse response;

        try {
            response = paymentApi.charge(paymentConfig.getApiKey(), amount);
        } catch (ExternalServiceException e) {
            // If external service fails, we want to ideally save the payment still
            // so that we can implement internal retry functions without having the customer
            // go through the process one more time
            paymentRepository.save(amount, PaymentStatus.FAILED.name());
            throw new PaymentException("External payment service error: ", e);
        }

        // save both failed and successful payments for audit
        if (!response.success()) {
            paymentRepository.save(amount, PaymentStatus.FAILED.name());
            throw new PaymentException("Payment failed with amount: " + amount);
        }

        paymentRepository.save(amount, PaymentStatus.SUCCESS.name());

        try {
            emailService.sendPaymentConfirmation(email, amount);
        } catch (NotificationException e) {
            // Continue if confirmation fails
        }

        return true;
    }
}
