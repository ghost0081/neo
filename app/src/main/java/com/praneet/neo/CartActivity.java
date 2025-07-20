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
    private CartManager cartManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Hide action bar if it exists
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        setContentView(R.layout.activity_cart);

        // Initialize CartManager
        cartManager = CartManager.getInstance(this);

        // Initialize views
        cartItemsContainer = findViewById(R.id.cart_items_container);
        totalPriceText = findViewById(R.id.total_price_text);
        emptyCartText = findViewById(R.id.empty_cart_text);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button proceedToPayment = findViewById(R.id.proceed_to_payment_button);
        proceedToPayment.setOnClickListener(v -> {
            List<CartItem> cartItems = cartManager.getCartItems();
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            double total = cartManager.getCartTotal();
            SupabaseManager.placeOrder(cartItems, total, new SupabaseManager.AuthCallback() {
                @Override
                public void onSuccess(String msg) {
                    runOnUiThread(() -> {
                        cartManager.clearCart();
                        loadCartItems();
                        Toast.makeText(CartActivity.this, "Order placed successfully!", Toast.LENGTH_LONG).show();
                    });
                }
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Toast.makeText(CartActivity.this, "Order failed: " + error, Toast.LENGTH_LONG).show());
                }
            });
        });

        // Load and display cart items
        loadCartItems();
        
        // Add debug button to test cart functionality
        // addDebugButton(); // Removed debug buttons
    }
    
    // Removed addDebugButton() method
    
    // Removed showQuickAddDialog() method

    private void loadCartItems() {
        List<CartItem> cartItems = cartManager.getCartItems();
        Log.d("CartActivity", "Loading cart items. Found: " + cartItems.size() + " items");
        
        if (cartItems.isEmpty()) {
            Log.d("CartActivity", "Cart is empty, showing empty state");
            showEmptyCart();
        } else {
            Log.d("CartActivity", "Cart has items, showing cart items");
            showCartItems(cartItems);
        }
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

        // Clear existing items
        cartItemsContainer.removeAllViews();

        // Add each cart item
        for (CartItem item : cartItems) {
            View cartItemView = createCartItemView(item);
            cartItemsContainer.addView(cartItemView);
        }

        // Update total price
        double total = cartManager.getCartTotal();
        totalPriceText.setText(String.format("Total: $%.2f", total));
    }

    private View createCartItemView(CartItem item) {
        // Root horizontal layout
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundColor(Color.WHITE);
        row.setPadding(16, 16, 16, 16);

        // Checkbox
        CheckBox checkBox = new CheckBox(this);
        checkBox.setChecked(true); // Default to selected, or bind to your selection logic
        LinearLayout.LayoutParams cbParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cbParams.setMargins(0, 0, 12, 0);
        checkBox.setLayoutParams(cbParams);

        // Image (rounded rectangle)
        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(64, 64));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setBackgroundResource(R.drawable.rounded_image_bg); // create a drawable with rounded corners
        imageView.setImageResource(android.R.drawable.ic_menu_gallery); // use your product image or a placeholder

        // Info (vertical)
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

        // Quantity controls (pill style)
        LinearLayout qty = new LinearLayout(this);
        qty.setOrientation(LinearLayout.HORIZONTAL);
        qty.setBackgroundResource(R.drawable.qty_pill_bg); // create a pill-shaped drawable
        qty.setPadding(8, 4, 8, 4);
        qty.setGravity(Gravity.CENTER);

        Button minus = new Button(this);
        minus.setText("–");
        minus.setBackgroundColor(Color.TRANSPARENT);
        minus.setTextSize(18);
        minus.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() - 1;
            cartManager.updateQuantity(item.getProduct().getId(), newQuantity);
            loadCartItems();
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
            cartManager.updateQuantity(item.getProduct().getId(), newQuantity);
            loadCartItems();
        });

        qty.addView(minus);
        qty.addView(quantity);
        qty.addView(plus);

        // Add all to row
        row.addView(checkBox);
        row.addView(imageView);
        row.addView(info);
        row.addView(qty);

        // Divider
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 1));
        divider.setBackgroundColor(Color.parseColor("#EEEEEE"));

        // Container for row + divider
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.addView(row);
        container.addView(divider);

        return container;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh cart items when returning to this activity
        loadCartItems();
    }
} 