package com.example.shop;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {
    private final List<Item> items = new ArrayList<>();
    private final List<Discount> discounts = new ArrayList<>();

    public ShoppingCart() {
    }

    public boolean addItem(Item item) {
        items.add(item);
        return true;
    }

    public void removeItem(Item item) {
        items.remove(item);
    }

    public List<Item> getItems() {
        return items;
    }

    public BigDecimal getTotalPrice() {
        BigDecimal total = items.stream()
                .map(Item::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Apply any discounts added to the cart (not from items)
        for (Discount discount : discounts) {
            total = discount.apply(total);
        }

        return total;
    }

    public void addDiscount(Discount discount) {
        discounts.add(discount);
    }
}
