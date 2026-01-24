package com.example.payment;

public interface PaymentApi {
    PaymentApiResponse charge(String apiKey, double amount);
}
