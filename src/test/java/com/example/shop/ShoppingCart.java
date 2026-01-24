package com.example.shop;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {
    private final List<Item> items = new ArrayList<>();

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
}
