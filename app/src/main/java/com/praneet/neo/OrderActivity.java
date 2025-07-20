package com.praneet.neo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ScrollView;
import android.view.Gravity;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;

public class OrderActivity extends AppCompatActivity {
    private LinearLayout ordersContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Hide action bar if it exists
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        setContentView(R.layout.activity_order);

        ordersContainer = findViewById(R.id.orders_container);
        loadOrders();
    }

    private void loadOrders() {
        SupabaseManager.getOrdersForCurrentUser(new SupabaseManager.OrdersCallback() {
            @Override
            public void onSuccess(JSONArray orders) {
                runOnUiThread(() -> {
                    if (orders.length() == 0) {
                        showEmptyMessage();
                        return;
                    }
                    showOrders(orders);
                });
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> showEmptyMessage());
            }
        });
    }

    private void showOrders(JSONArray orders) {
        ordersContainer.removeAllViews();
        if (orders.length() == 0) {
            showEmptyMessage();
            return;
        }
        for (int i = 0; i < orders.length(); i++) {
            try {
                JSONObject order = orders.getJSONObject(i);
                long orderId = order.getLong("id");
                String createdAt = order.optString("created_at", "");
                String status = order.optString("status", "");
                double total = order.optDouble("total_price", 0.0);

                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setBackgroundColor(0xFFFFFFFF);
                card.setPadding(32, 32, 32, 32);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 0, 32);
                card.setLayoutParams(params);
                card.setElevation(8f);

                TextView orderIdView = new TextView(this);
                orderIdView.setText("Order #" + orderId);
                orderIdView.setTextSize(16);
                orderIdView.setTypeface(null, Typeface.BOLD);
                orderIdView.setTextColor(0xFF222222);

                TextView dateView = new TextView(this);
                dateView.setText("Date: " + createdAt.replace("T", " ").replace(".000Z", ""));
                dateView.setTextSize(14);
                dateView.setTextColor(0xFF888888);

                TextView statusView = new TextView(this);
                statusView.setText("Status: " + status);
                statusView.setTextSize(14);
                statusView.setTextColor(0xFF4CAF50);

                TextView totalView = new TextView(this);
                totalView.setText("Total: $" + String.format("%.2f", total));
                totalView.setTextSize(15);
                totalView.setTypeface(null, Typeface.BOLD);
                totalView.setTextColor(0xFF222222);
                totalView.setPadding(0, 8, 0, 0);

                card.addView(orderIdView);
                card.addView(dateView);
                card.addView(statusView);
                card.addView(totalView);

                // Fetch and show order items
                SupabaseManager.getOrderItems(orderId, new SupabaseManager.OrderItemsCallback() {
                    @Override
                    public void onSuccess(JSONArray orderItems) {
                        runOnUiThread(() -> showOrderItems(card, orderItems));
                    }
                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            TextView errorView = new TextView(OrderActivity.this);
                            errorView.setText("Failed to load items");
                            errorView.setTextColor(0xFFFF0000);
                            card.addView(errorView);
                        });
                    }
                });

                ordersContainer.addView(card);
            } catch (Exception e) {
                // skip
            }
        }
    }

    private void showOrderItems(LinearLayout card, JSONArray orderItems) {
        for (int i = 0; i < orderItems.length(); i++) {
            try {
                JSONObject item = orderItems.getJSONObject(i);
                long productId = item.getLong("product_id");
                int quantity = item.getInt("quantity");
                double price = item.getDouble("price_at_purchase");

                TextView itemView = new TextView(this);
                itemView.setText("• Product ID: " + productId + "  Qty: " + quantity + "  Price: $" + String.format("%.2f", price));
                itemView.setTextSize(14);
                itemView.setTextColor(0xFF444444);
                itemView.setPadding(0, 4, 0, 0);
                card.addView(itemView);
            } catch (Exception e) {
                // skip
            }
        }
    }

    private void showEmptyMessage() {
        ordersContainer.removeAllViews();
        TextView empty = new TextView(this);
        empty.setText("No orders yet.");
        empty.setTextSize(18);
        empty.setTextColor(0xFF888888);
        empty.setGravity(Gravity.CENTER);
        ordersContainer.addView(empty);
    }
} 