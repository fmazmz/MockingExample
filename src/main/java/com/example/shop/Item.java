package com.example.shop;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Item {
    private final String id;
    private final BigDecimal price;
    private int quantity;
    private final List<Discount> discounts = new ArrayList<>();

    public Item(String id, BigDecimal price, int quantity) {
        this.id = id;
        this.price = price;
        setQuantity(quantity);
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

    private void setQuantity(int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("Quantity must be at least 1");
        this.quantity = quantity;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public void increaseQuantity(int amount) {
        if (amount < 1) throw new IllegalArgumentException("Amount must be positive");
        this.quantity += amount;
    }

    public void decreaseQuantity(int amount) {
        if (amount < 1) throw new IllegalArgumentException("Amount must be positive");
        if (quantity - amount < 1) throw new IllegalArgumentException("Quantity cannot go below 1");
        this.quantity -= amount;
    }
}
