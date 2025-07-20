package com.praneet.neo;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.praneet.neo.model.CartItem;
import com.praneet.neo.model.Product;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static final String TAG = "CartManager";
    private static final String PREF_NAME = "cart_preferences";
    private static final String CART_ITEMS_KEY = "cart_items";
    
    private static CartManager instance;
    private List<CartItem> cartItems;
    private Gson gson;
    private Context context;
    
    private CartManager(Context context) {
        this.context = context.getApplicationContext();
        this.gson = new Gson();
        this.cartItems = new ArrayList<>();
        loadCartItems();
    }
    
    public static synchronized CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context);
        }
        return instance;
    }
    
    // Add item to cart
    public void addToCart(Product product) {
        addToCart(product, 1);
    }
    
    public void addToCart(Product product, int quantity) {
        Log.d(TAG, "Adding product to cart: " + product.getTitle() + " (ID: " + product.getId() + ")");
        Log.d(TAG, "Current cart items before adding: " + cartItems.size());
        
        // Check if product already exists in cart
        for (CartItem item : cartItems) {
            if (item.getProduct().getId() == product.getId()) {
                // Update quantity
                item.setQuantity(item.getQuantity() + quantity);
                saveCartItems();
                Log.d(TAG, "Updated quantity for " + product.getTitle() + " to " + item.getQuantity());
                return;
            }
        }
        
        // Add new item
        CartItem newItem = new CartItem(product, quantity);
        cartItems.add(newItem);
        saveCartItems();
        Log.d(TAG, "Added " + product.getTitle() + " to cart. Total items now: " + cartItems.size());
    }
    
    // Remove item from cart
    public void removeFromCart(int productId) {
        cartItems.removeIf(item -> item.getProduct().getId() == productId);
        saveCartItems();
        Log.d(TAG, "Removed product with ID " + productId + " from cart");
    }
    
    // Update item quantity
    public void updateQuantity(int productId, int quantity) {
        for (CartItem item : cartItems) {
            if (item.getProduct().getId() == productId) {
                if (quantity <= 0) {
                    removeFromCart(productId);
                } else {
                    item.setQuantity(quantity);
                    saveCartItems();
                }
                return;
            }
        }
    }
    
    // Get all cart items
    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }
    
    // Get cart item count
    public int getCartItemCount() {
        int totalItems = 0;
        for (CartItem item : cartItems) {
            totalItems += item.getQuantity();
        }
        return totalItems;
    }
    
    // Get cart total price
    public double getCartTotal() {
        double total = 0.0;
        for (CartItem item : cartItems) {
            total += item.getProduct().getPrice() * item.getQuantity();
        }
        return total;
    }
    
    // Clear cart
    public void clearCart() {
        cartItems.clear();
        saveCartItems();
        Log.d(TAG, "Cart cleared");
    }
    
    // Check if cart is empty
    public boolean isCartEmpty() {
        return cartItems.isEmpty();
    }
    
    // Save cart items to SharedPreferences
    private void saveCartItems() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = gson.toJson(cartItems);
        prefs.edit().putString(CART_ITEMS_KEY, json).apply();
        Log.d(TAG, "Cart items saved: " + cartItems.size() + " items");
        Log.d(TAG, "Saved JSON: " + json);
    }
    
    // Load cart items from SharedPreferences
    private void loadCartItems() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(CART_ITEMS_KEY, "[]");
        Log.d(TAG, "Loading cart items from JSON: " + json);
        
        try {
            Type type = new TypeToken<ArrayList<CartItem>>(){}.getType();
            List<CartItem> loadedItems = gson.fromJson(json, type);
            if (loadedItems != null) {
                cartItems = loadedItems;
                Log.d(TAG, "Cart items loaded successfully: " + cartItems.size() + " items");
            } else {
                Log.d(TAG, "Loaded items is null, creating empty list");
                cartItems = new ArrayList<>();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading cart items: " + e.getMessage());
            e.printStackTrace();
            cartItems = new ArrayList<>();
        }
    }
} 