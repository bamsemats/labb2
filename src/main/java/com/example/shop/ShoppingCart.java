package com.example.shop;

import java.util.HashSet;
import java.util.Set;

public class ShoppingCart {
    private final String cartID;
    private final String cartName;
    private final Set<CartItem> cartItems = new HashSet<>();

    public ShoppingCart(String cartID, String cartName) {
        this.cartID = cartID;
        this.cartName = cartName;
    }
    public Boolean findById(String id) {
        return id.equals(cartID);
    }
    public boolean addItem(CartItem cartItem) {
        cartItems.add(cartItem);
        return true;
    }
    public boolean removeItem(CartItem cartItem) {
        cartItems.remove(cartItem);
        return true;
    }
    public Double getTotalPrice() {
        Double result = 0.0;

        for (CartItem cartItem : cartItems) {
            result += cartItem.price * cartItem.quantity;
        }
        return result;
    }
    public Double getDiscountedPrice(Double discount) {
        Double result = 0.0;

        for (CartItem cartItem : cartItems) {
            result += cartItem.price * cartItem.quantity;
        }
        return result * discount;
    }
}
