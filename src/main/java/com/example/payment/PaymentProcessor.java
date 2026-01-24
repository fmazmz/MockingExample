package com.example.payment;

import java.math.BigDecimal;

public class PaymentProcessor {
    private final String apiKey;
    private final PaymentRepository paymentRepository;
    private final PaymentApi paymentApi;
    private final EmailService emailService;

    public PaymentProcessor(
            String apiKey,
            PaymentRepository paymentRepository,
            PaymentApi paymentApi,
            EmailService emailService) {
        this.apiKey = apiKey;
        this.paymentRepository = paymentRepository;
        this.paymentApi = paymentApi;
        this.emailService = emailService;
    }

    public boolean processPayment(String email, BigDecimal amount) throws PaymentException {
        // Anropar extern betaltjänst
        PaymentApiResponse response = paymentApi.charge(apiKey, amount);

        // save both failed and successful payments for audit
        if (!response.success()) {
            paymentRepository.save(amount, PaymentStatus.FAILED.name());
            throw new PaymentException("Payment failed with amount: " + amount);
        }

        paymentRepository.save(amount, PaymentStatus.SUCCESS.name());
        emailService.sendPaymentConfirmation(email, amount);

        return true;
    }
}
