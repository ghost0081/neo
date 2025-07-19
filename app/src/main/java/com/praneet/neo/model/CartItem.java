package com.praneet.neo.model;

public class CartItem {
    private Product product;
    private int quantity;

    public CartItem(Product product) {
        this(product, 1);
    }

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public CartItem copy(int quantity) {
        return new CartItem(this.product, quantity);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CartItem cartItem = (CartItem) obj;
        return product.getId() == cartItem.product.getId();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(product.getId());
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "product=" + product +
                ", quantity=" + quantity +
                '}';
    }
} 