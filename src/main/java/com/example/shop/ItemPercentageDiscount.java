package com.example.shop;

import java.math.BigDecimal;

public class ItemPercentageDiscount implements Discount {

    private final BigDecimal percentage;

    public ItemPercentageDiscount(BigDecimal percentage) {
        if (percentage.compareTo(BigDecimal.ZERO) < 0 || percentage.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException(
                    "Discount must be a percentage in double format between 0 and 1 (e.g. 0.25)"
            );
        }
        this.percentage = percentage;
    }

    @Override
    public BigDecimal apply(BigDecimal originalPrice) {
        return originalPrice.multiply(BigDecimal.ONE.subtract(percentage));
    }
}