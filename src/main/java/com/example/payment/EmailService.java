package com.example.payment;

import com.example.NotificationException;

import java.math.BigDecimal;

public interface EmailService {
    void sendPaymentConfirmation(String email, BigDecimal amount) throws NotificationException;
}
