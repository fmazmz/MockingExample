package com.example.payment;


import java.math.BigDecimal;

public interface PaymentRepository {
    void save(BigDecimal amount, String status);
}
