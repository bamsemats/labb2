package com.example.shop;

public class CartItem
{
    private final String id;
    private final String name;
    public int price;
    public int quantity;

    public  CartItem(String id, String name, int price, int quantity) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int setQuantity(int quantity) {
        this.quantity = quantity;
        return quantity;
    }

    public int getQuantity() {
        return quantity;
    }
}
