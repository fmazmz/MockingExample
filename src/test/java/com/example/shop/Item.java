package com.example.shop;

import java.math.BigDecimal;

public class Item {
    private String name;
    private BigDecimal price;

    public Item(String name, BigDecimal price) {
        this.name = name;
        this.price = price;
    }
}
