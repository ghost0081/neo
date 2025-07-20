package com.praneet.neo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.praneet.neo.model.CartItem;
import com.praneet.neo.model.Product;
import java.util.List;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;

public class CartActivity extends AppCompatActivity {
    private LinearLayout cartItemsContainer;
    private TextView totalPriceText;
    private TextView emptyCartText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide action bar if it exists
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_cart);
        cartItemsContainer = findViewById(R.id.cart_items_container);
        totalPriceText = findViewById(R.id.total_price_text);
        emptyCartText = findViewById(R.id.empty_cart_text);
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
        Button proceedToPayment = findViewById(R.id.proceed_to_payment_button);
        proceedToPayment.setOnClickListener(v -> placeOrder());
        loadCartItems();
    }

    private void loadCartItems() {
        CartManager.getCartItems(new SupabaseManager.CartItemsCallback() {
            @Override
            public void onSuccess(List<CartItem> cartItems) {
                runOnUiThread(() -> {
                    if (cartItems == null || cartItems.isEmpty()) {
                        showEmptyCart();
                    } else {
                        showCartItems(cartItems);
                    }
                });
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> showEmptyCart());
            }
        });
    }

    private void placeOrder() {
        CartManager.getCartItems(new SupabaseManager.CartItemsCallback() {
            @Override
            public void onSuccess(List<CartItem> cartItems) {
                if (cartItems == null || cartItems.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(CartActivity.this, "Cart is empty", Toast.LENGTH_SHORT).show());
                    return;
                }
                double total = 0.0;
                for (CartItem item : cartItems) {
                    total += item.getProduct().getPrice() * item.getQuantity();
                }
                SupabaseManager.placeOrder(cartItems, total, new SupabaseManager.AuthCallback() {
                    @Override
                    public void onSuccess(String msg) {
                        runOnUiThread(() -> {
                            CartManager.clearCart(new SupabaseManager.CartCallback() {
                                @Override
                                public void onSuccess() {
                                    loadCartItems();
                                    Toast.makeText(CartActivity.this, "Order placed successfully!", Toast.LENGTH_LONG).show();
                                }
                                @Override
                                public void onError(String error) {
                                    loadCartItems();
                                    Toast.makeText(CartActivity.this, "Order placed, but failed to clear cart.", Toast.LENGTH_LONG).show();
                                }
                            });
                        });
                    }
                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> Toast.makeText(CartActivity.this, "Order failed: " + error, Toast.LENGTH_LONG).show());
                    }
                });
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(CartActivity.this, "Failed to fetch cart: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showEmptyCart() {
        cartItemsContainer.setVisibility(View.GONE);
        totalPriceText.setVisibility(View.GONE);
        emptyCartText.setVisibility(View.VISIBLE);
    }

    private void showCartItems(List<CartItem> cartItems) {
        cartItemsContainer.setVisibility(View.VISIBLE);
        totalPriceText.setVisibility(View.VISIBLE);
        emptyCartText.setVisibility(View.GONE);
        cartItemsContainer.removeAllViews();
        double total = 0.0;
        for (CartItem item : cartItems) {
            View cartItemView = createCartItemView(item);
            cartItemsContainer.addView(cartItemView);
            total += item.getProduct().getPrice() * item.getQuantity();
        }
        totalPriceText.setText(String.format("Total: $%.2f", total));
    }

    private View createCartItemView(CartItem item) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundColor(Color.WHITE);
        row.setPadding(16, 16, 16, 16);
        // Checkbox
        CheckBox checkBox = new CheckBox(this);
        checkBox.setChecked(true);
        LinearLayout.LayoutParams cbParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cbParams.setMargins(0, 0, 12, 0);
        checkBox.setLayoutParams(cbParams);
        // Image
        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(64, 64));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setBackgroundResource(R.drawable.rounded_image_bg);
        imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        // Info
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(16, 0, 0, 0);
        info.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        TextView name = new TextView(this);
        name.setText(item.getProduct().getTitle());
        name.setTextSize(15);
        name.setTypeface(null, Typeface.BOLD);
        name.setTextColor(Color.BLACK);
        name.setMaxLines(2);
        name.setEllipsize(TextUtils.TruncateAt.END);
        TextView price = new TextView(this);
        price.setText(String.format("£%.2f", item.getProduct().getPrice()));
        price.setTextSize(15);
        price.setTextColor(Color.parseColor("#222222"));
        price.setPadding(0, 8, 0, 0);
        info.addView(name);
        info.addView(price);
        // Quantity controls
        LinearLayout qty = new LinearLayout(this);
        qty.setOrientation(LinearLayout.HORIZONTAL);
        qty.setBackgroundResource(R.drawable.qty_pill_bg);
        qty.setPadding(8, 4, 8, 4);
        qty.setGravity(Gravity.CENTER);
        Button minus = new Button(this);
        minus.setText("–");
        minus.setBackgroundColor(Color.TRANSPARENT);
        minus.setTextSize(18);
        minus.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() - 1;
            CartManager.updateQuantity(item.getProduct().getId(), newQuantity, new SupabaseManager.CartCallback() {
                @Override
                public void onSuccess() { loadCartItems(); }
                @Override
                public void onError(String error) { loadCartItems(); }
            });
        });
        TextView quantity = new TextView(this);
        quantity.setText(String.valueOf(item.getQuantity()));
        quantity.setTextSize(16);
        quantity.setPadding(12, 0, 12, 0);
        Button plus = new Button(this);
        plus.setText("+");
        plus.setBackgroundColor(Color.TRANSPARENT);
        plus.setTextSize(18);
        plus.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() + 1;
            CartManager.updateQuantity(item.getProduct().getId(), newQuantity, new SupabaseManager.CartCallback() {
                @Override
                public void onSuccess() { loadCartItems(); }
                @Override
                public void onError(String error) { loadCartItems(); }
            });
        });
        qty.addView(minus);
        qty.addView(quantity);
        qty.addView(plus);
        row.addView(checkBox);
        row.addView(imageView);
        row.addView(info);
        row.addView(qty);
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(Color.parseColor("#EEEEEE"));
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.addView(row);
        container.addView(divider);
        return container;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCartItems();
    }
} 