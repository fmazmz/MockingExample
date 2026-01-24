package com.example;

import com.example.shop.Item;
import com.example.shop.ItemPercentageDiscount;
import com.example.shop.ShoppingCart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    @DisplayName("removes item from the cart")
    @Test
    void removeItemFromCart() {
        Item item = new Item("item1", BigDecimal.valueOf(100.0));
        cart.addItem(item);

        // Confirm item is stored in the cart
        assertThat(cart.getItems()).containsExactly(item);

        cart.removeItem(item);

        // Confirm item has been removed from the cart
        assertThat(cart.getItems()).isEmpty();
    }

    @DisplayName("calculates total sum of cart items")
    @Test
    void calculatesPriceOfCartItems() {
        cart.addItem(new Item("item1", BigDecimal.valueOf(100.0)));
        cart.addItem(new Item("item2", BigDecimal.valueOf(100.0)));
        cart.addItem(new Item("item3", BigDecimal.valueOf(100.0)));

        BigDecimal actualTotalPrice = cart.getTotalPrice();

        assertThat(actualTotalPrice).isEqualByComparingTo(BigDecimal.valueOf(300));
    }

    @DisplayName("applies discount on an item")
    @Test
    void applyDiscountOnStandaloneItem() {
        Item item = new Item("item", BigDecimal.valueOf(300.0));
        item.addDiscount(new ItemPercentageDiscount(BigDecimal.valueOf(0.25)));

        cart.addItem(item);
        assertThat(cart.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(225.0));
    }
}
