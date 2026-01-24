package com.example.shop;

import java.math.BigDecimal;

public class ItemPercentageDiscount implements Discount {

    private final BigDecimal percentage;

    public ItemPercentageDiscount(BigDecimal percentage) {
        this.percentage = percentage;
    }

    @Override
    public BigDecimal apply(BigDecimal originalPrice) {
        return originalPrice.multiply(BigDecimal.ONE.subtract(percentage));
    }
}