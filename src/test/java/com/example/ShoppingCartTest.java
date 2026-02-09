package com.example;

import com.example.shop.CartItem;
import com.example.shop.ShoppingCart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ShoppingCartTest {

    @InjectMocks
    private ShoppingCart shoppingCartImpl;
    private final String testItemId = "testItemId";
    private final String testItemName = "testItemName";
    private final int testItemPrice = 10;
    private final int testItemQuantity = 2;
    private final String cartId = "cart1";
    private final Double discount = 0.0;


    @BeforeEach
    public void setup() {
        shoppingCartImpl = new ShoppingCart(cartId, "Test cart");
    }

    @Nested
    @DisplayName("handleItems() Tests")
    class HandleItemsTests {

        @Test
        @DisplayName("Should succeed for available cart")
        void shouldSucceedForAvailableCart() {
            boolean result = shoppingCartImpl.findById(cartId);
            assertThat(result).isTrue();
        }

        // Kant-test: null ID
        @Test
        @DisplayName("Should return exception for null ID")
        void shouldReturnExceptionForNullId() {
            assertThatThrownBy(() -> shoppingCartImpl.findById(null)).isInstanceOf(IllegalArgumentException.class).hasMessage("Invalid cart ID");
        }

         @Test
         @DisplayName("Should successfully add items")
         void shouldSuccessfullyAddItems() {
            CartItem test = new CartItem(testItemId, testItemName, testItemPrice, testItemQuantity);

            boolean result = shoppingCartImpl.addItem(test);
            assertThat(result).isTrue();
         }

         @Test
         @DisplayName("Should return exception for incorrect input for cartItem")
         void shouldReturnExceptionForIncorrectInput() {
            assertThatThrownBy(() -> shoppingCartImpl.addItem(
                    new CartItem(testItemId, testItemName, testItemPrice, -1)))
                    .isInstanceOf(IllegalArgumentException.class).hasMessage("Invalid input");
            assertThatThrownBy(() -> shoppingCartImpl.addItem(
                    new CartItem(testItemId, testItemName, -1, testItemQuantity)))
                    .isInstanceOf(IllegalArgumentException.class).hasMessage("Invalid input");
         }

         @Test
         @DisplayName("Should successfully remove items")
         void shouldSuccessfullyRemoveItems() {
            CartItem test = new CartItem(testItemId, testItemName, testItemPrice, testItemQuantity);
            shoppingCartImpl.addItem(test);
            boolean result = shoppingCartImpl.removeItem(test);
            assertThat(result).isTrue();
         }

         @Test
         @DisplayName("Should throw exception if item does not exist")
         void shouldThrowExceptionIfItemDoesNotExist() {
            assertThatThrownBy(() -> shoppingCartImpl.removeItem(
                    new CartItem(testItemId, testItemName, testItemPrice, testItemQuantity)))
                    .isInstanceOf(IllegalArgumentException.class).hasMessage("Item does not exist");
         }

         @Test
        @DisplayName("Should display total price")
        void shouldDisplayTotalPrice() {
            ShoppingCart testCart = new ShoppingCart(cartId, "Test cart");
            CartItem test = new CartItem(testItemId, testItemName, testItemPrice, testItemQuantity);
            testCart.addItem(test);
            Double result = testCart.getTotalPrice();
            assertThat(result).isEqualTo(test.price * test.quantity);
         }

         @Test
        @DisplayName("Should display total price with discount")
        void shouldDisplayTotalPriceWithDiscount() {
            ShoppingCart testCart = new ShoppingCart(cartId, "Test cart");
            CartItem test = new CartItem(testItemId, testItemName, testItemPrice, testItemQuantity);
            testCart.addItem(test);
            Double result = testCart.getDiscountedPrice(discount);
            assertThat(result).isEqualTo(test.price * test.quantity * discount);
         }

         @Test
        @DisplayName("Should apply quantity changes")
        void shouldApplyQuantityChanges() {
            ShoppingCart testCart = new ShoppingCart(cartId, "Test cart");
            CartItem test = new CartItem(testItemId, testItemName, testItemPrice, testItemQuantity);
            testCart.addItem(test);
            testCart.updateQuantity(test, 5);
            int result = testCart.checkQuantity(test);
            assertThat(result).isEqualTo(5);
         }

         @Test
        @DisplayName("Should throw exception if new quantity is below zero")
        void  shouldThrowExceptionIfNewQuantityIsBelowZero() {
             ShoppingCart testCart = new ShoppingCart(cartId, "Test cart");
             CartItem test = new CartItem(testItemId, testItemName, testItemPrice, testItemQuantity);
             testCart.addItem(test);
             assertThatThrownBy(() -> testCart.updateQuantity(test, -1))
                     .isInstanceOf(IllegalArgumentException.class)
                     .hasMessage("Invalid input");
         }
    }



}
