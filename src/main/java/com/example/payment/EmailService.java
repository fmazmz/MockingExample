package com.example.payment;

import java.math.BigDecimal;

public interface EmailService {
    void sendPaymentConfirmation(String email, BigDecimal amount);
}
