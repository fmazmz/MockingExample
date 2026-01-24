package com.example;

import com.example.shop.CartPercentageDiscount;
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
        Item item = new Item("name", BigDecimal.valueOf(250.0), 1);
        cart.addItem(item);
        assertThat(cart.getItems()).hasSize(1);
    }

    @DisplayName("stores added item in the cart")
    @Test
    void cartContainsItem() {
        Item item = new Item("name", BigDecimal.valueOf(250.0), 1);
        cart.addItem(item);

        List<Item> cartItems = cart.getItems();

        assertThat(cartItems).containsExactly(item);
    }

    @DisplayName("removes item from the cart")
    @Test
    void removeItemFromCart() {
        Item item = new Item("item1", BigDecimal.valueOf(100.0), 1);
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
        cart.addItem(new Item("item1", BigDecimal.valueOf(100.0), 1));
        cart.addItem(new Item("item2", BigDecimal.valueOf(100.0), 1));
        cart.addItem(new Item("item3", BigDecimal.valueOf(100.0), 1));

        BigDecimal actualTotalPrice = cart.getTotalPrice();

        assertThat(actualTotalPrice).isEqualByComparingTo(BigDecimal.valueOf(300));
    }

    @DisplayName("applies discount on an item")
    @Test
    void applyDiscountOnStandaloneItem() {
        Item item = new Item("item", BigDecimal.valueOf(300.0), 1);
        item.addDiscount(new ItemPercentageDiscount(BigDecimal.valueOf(0.25)));

        cart.addItem(item);
        assertThat(cart.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(225.0));
    }

    @DisplayName("applies discount on an item and a cart")
    @Test
    void applyDiscountOnItemAndCart() {
        Item item1 = new Item("item1", BigDecimal.valueOf(100.0), 1);
        Item item2 = new Item("item2", BigDecimal.valueOf(100.0), 1);

        item1.addDiscount(new ItemPercentageDiscount(BigDecimal.valueOf(0.10)));

        cart.addItem(item1);
        cart.addItem(item2);

        // Add a discount on the total cart price on top of the discounted item
        cart.addDiscount(new CartPercentageDiscount(BigDecimal.valueOf(0.10)));

        // Verify standalone item 1 is discounted with 10%
        assertThat(item1.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(90.0));
        // Verify standalone item 2 is NOT discounted and has original price
        assertThat(item2.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(100.0));

        // Verify cart discount has been applied on its total price
        assertThat(cart.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(171.0));
    }

    @DisplayName("add multiple quantities of an item")
    @Test
    void addMultipleOfSameItem() {
        Item item = new Item("item", BigDecimal.valueOf(100.0), 3);
        cart.addItem(item);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().getFirst().getQuantity()).isEqualTo(3);

        // add another 2 of the same item
        item.increaseQuantity(2);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().getFirst().getQuantity()).isEqualTo(5);
    }

    @DisplayName("decrease quantity of an item")
    @Test
    void decreaseItemQuantity() {
        Item item = new Item("item", BigDecimal.valueOf(100.0), 3);
        cart.addItem(item);

        item.decreaseQuantity(1);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().getFirst().getQuantity()).isEqualTo(2);
    }
}
