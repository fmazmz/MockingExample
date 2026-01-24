package com.example.payment;

import java.math.BigDecimal;

public interface PaymentApi {
    PaymentApiResponse charge(String apiKey, BigDecimal amount);
}
