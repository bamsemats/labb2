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
        if (cartID.equals(null) || !cartID.equals(id)) {
            throw new IllegalArgumentException("Invalid cart ID");
        }
        return id.equals(cartID);
    }
    public boolean addItem(CartItem cartItem) {
        if (cartItem.getId() == null || cartItem.getName() == null || cartItem.getPrice() < 0 || cartItem.getQuantity() < 0) {
            throw new IllegalArgumentException("Invalid input");
        } else {
            cartItems.add(cartItem);
            return true;
        }
    }
    public boolean removeItem(CartItem cartItem) {
        if (!cartItems.contains(cartItem)) {
            throw new IllegalArgumentException("Item does not exist");
        } else {
            cartItems.remove(cartItem);
            return true;
        }
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


    public void updateQuantity(CartItem item, int i) {
        if (i < 0) {
            throw new IllegalArgumentException("Invalid input");
        }
        for (CartItem cartItem : cartItems) {
            if (cartItem.getId().equals(item.getId()) && cartItem.quantity == i) {
                return;
            } else if (cartItem.getId().equals(item.getId())) {
                item.setQuantity(i);
            }
        }
    }

    public int checkQuantity(CartItem item) {
        int result = 0;
        for (CartItem cartItem : cartItems) {
            if (cartItem.getId().equals(item.getId())) {
                result = cartItem.quantity;
            }
        }
        return result;
    }
}
