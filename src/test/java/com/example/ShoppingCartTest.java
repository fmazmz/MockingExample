package com.example;

import com.example.shop.Item;
import com.example.shop.ShoppingCart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class ShoppingCartTest {

    private ShoppingCart cart;

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart();
    }

    @Test
    void addItemToCart() {
        Item item = new Item("name", BigDecimal.valueOf(250.0));
        boolean result = cart.addItem(item);
        assertThat(result).isTrue();}
}
