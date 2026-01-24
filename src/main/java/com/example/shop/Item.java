package com.example.shop;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Item {
    private final String id;
    private final BigDecimal price;
    private final List<Discount> discounts = new ArrayList<>();

    public Item(String name, BigDecimal price) {
        this.id = name;
        this.price = price;
    }

    public BigDecimal getPrice() {
        BigDecimal discountedPrice = price;
        for (Discount discount : discounts) {
            discountedPrice = discount.apply(discountedPrice);
        }
        return discountedPrice;
    }

    public void addDiscount(Discount discount) {
        discounts.add(discount);
    }
}
