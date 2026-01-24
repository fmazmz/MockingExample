package com.example;

import com.example.shop.Item;
import com.example.shop.ShoppingCart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ShoppingCartTest {

    private ShoppingCart cart;

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart();
    }

    @DisplayName("adds an item to the cart")
    @Test
    void addItemToCart() {
        Item item = new Item("name", BigDecimal.valueOf(250.0));
        boolean result = cart.addItem(item);
        assertThat(result).isTrue();
    }

    @DisplayName("stores added item in the cart")
    @Test
    void cartContainsItem() {
        Item item = new Item("name", BigDecimal.valueOf(250.0));
        cart.addItem(item);

        List<Item> cartItems = cart.getItems();

        assertThat(cartItems).containsExactly(item);
    }
}
