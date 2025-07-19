package com.praneet.neo.viewmodel;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;
import com.praneet.neo.model.CartItem;
import com.praneet.neo.model.Product;
import java.util.ArrayList;
import java.util.List;

public class CartViewModel extends ViewModel {
    private MutableLiveData<List<CartItem>> cartItems = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<CartItem>> getCartItems() {
        return cartItems;
    }

    public void addToCart(Product product) {
        List<CartItem> current = new ArrayList<>(cartItems.getValue());
        int index = -1;
        for (int i = 0; i < current.size(); i++) {
            if (current.get(i).getProduct().getId() == product.getId()) {
                index = i;
                break;
            }
        }
        
        if (index >= 0) {
            CartItem existingItem = current.get(index);
            current.set(index, existingItem.copy(existingItem.getQuantity() + 1));
        } else {
            current.add(new CartItem(product));
        }
        cartItems.setValue(current);
    }

    public void updateQuantity(int productId, int quantity) {
        List<CartItem> current = new ArrayList<>(cartItems.getValue());
        int index = -1;
        for (int i = 0; i < current.size(); i++) {
            if (current.get(i).getProduct().getId() == productId) {
                index = i;
                break;
            }
        }
        
        if (index >= 0 && quantity > 0) {
            current.set(index, current.get(index).copy(quantity));
        } else if (index >= 0 && quantity == 0) {
            current.remove(index);
        }
        cartItems.setValue(current);
    }

    public double getTotal() {
        double total = 0.0;
        List<CartItem> items = cartItems.getValue();
        if (items != null) {
            for (CartItem item : items) {
                total += item.getProduct().getPrice() * item.getQuantity();
            }
        }
        return total;
    }
} 