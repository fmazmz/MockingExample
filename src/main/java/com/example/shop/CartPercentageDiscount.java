package com.example.shop;

import java.math.BigDecimal;

public class CartPercentageDiscount implements Discount {
    private final BigDecimal percentage;

    public CartPercentageDiscount(BigDecimal percentage) {
        this.percentage = percentage;
    }

    @Override
    public BigDecimal apply(BigDecimal originalPrice) {
        return originalPrice.multiply(BigDecimal.ONE.subtract(percentage));
    }
}
