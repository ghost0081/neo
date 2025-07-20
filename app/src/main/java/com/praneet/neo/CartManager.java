package com.praneet.neo;

import android.util.Log;
import com.praneet.neo.model.CartItem;
import com.praneet.neo.model.Product;
import java.util.List;

public class CartManager {
    private static final String TAG = "CartManager";

    // Add item to cart (backend only)
    public static void addToCart(Product product, int quantity, SupabaseManager.CartCallback callback) {
        SupabaseManager.addToCart(product, quantity, callback);
    }

    // Remove item from cart (backend only)
    public static void removeFromCart(int productId, SupabaseManager.CartCallback callback) {
        SupabaseManager.removeFromCart(productId, callback);
    }

    // Update item quantity (backend only)
    public static void updateQuantity(int productId, int quantity, SupabaseManager.CartCallback callback) {
        SupabaseManager.updateCartQuantity(productId, quantity, callback);
    }

    // Get all cart items (backend only)
    public static void getCartItems(SupabaseManager.CartItemsCallback callback) {
        SupabaseManager.getCartItems(callback);
    }

    // Clear cart (backend only)
    public static void clearCart(SupabaseManager.CartCallback callback) {
        SupabaseManager.clearCart(callback);
    }
} 