package com.example;

import com.example.shop.CartItem;
import com.example.shop.ShoppingCart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ShoppingCartTest {
    @Mock
    private ShoppingCart shoppingCart;

    @InjectMocks
    private ShoppingCart shoppingCartImpl;
    private final String testItemId = "testItemId";
    private final String testItemName = "testItemName";
    private final String testItem = "testItem1";
    private final int itemQuantity = 0;
    private final String cartId = "cart1";
    private final String shoppingId = "shopping1";
    private final Double discount = 0.0;


    @BeforeEach
    public void setup() {
        shoppingCartImpl = new ShoppingCart(cartId, "Test cart");
    }

    @Nested
    @DisplayName("addItem() Tests")
    class AddItemTests {

        @Test
        @DisplayName("Should succeed for available cart")
        void shouldSucceedForAvailableCart() {
            boolean result = shoppingCartImpl.findById(cartId);
            assertThat(result).isTrue();
        }

         @Test
         @DisplayName("Should successfully add items")
         void shouldSuccessfullyAddItems() {
            CartItem test = new CartItem(testItemId, testItemName);

            boolean result = shoppingCartImpl.addItem(test);
            assertThat(result).isTrue();
         }
    }



}
