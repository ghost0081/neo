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
import com.praneet.neo.model.Product;
import com.praneet.neo.repository.ProductRepository;
import com.praneet.neo.repository.ProductRepositoryImpl;
import java.util.List;
import java.util.ArrayList;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.content.Intent;
import android.graphics.Color;
// If you have Glide, import it:
// import com.bumptech.glide.Glide;

public class FavoriteActivity extends AppCompatActivity {
    private LinearLayout favoritesContainer;
    private ProductRepository productRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Hide action bar if it exists
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        setContentView(R.layout.activity_favorite);

        favoritesContainer = findViewById(R.id.favorites_container);
        productRepository = new ProductRepositoryImpl();
        loadFavorites();
    }

    private void loadFavorites() {
        SupabaseManager.getFavorites(new SupabaseManager.FavoritesCallback() {
            @Override
            public void onSuccess(List<Long> productIds) {
                runOnUiThread(() -> {
                    if (productIds.isEmpty()) {
                        showEmptyMessage();
                        return;
                    }
                    fetchFavoriteProducts(productIds);
                });
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> showEmptyMessage());
            }
        });
    }

    private void fetchFavoriteProducts(List<Long> productIds) {
        new Thread(() -> {
            try {
                List<Product> allProducts = productRepository.getProducts();
                List<Product> favProducts = new ArrayList<>();
                for (Product p : allProducts) {
                    if (productIds.contains((long)p.getId())) {
                        favProducts.add(p);
                    }
                }
                runOnUiThread(() -> showFavorites(favProducts));
            } catch (Exception e) {
                runOnUiThread(() -> showEmptyMessage());
            }
        }).start();
    }

    private void showFavorites(List<Product> products) {
        favoritesContainer.removeAllViews();
        if (products.isEmpty()) {
            showEmptyMessage();
            return;
        }
        for (Product product : products) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setBackgroundColor(0xFFFFFFFF);
            card.setPadding(32, 32, 32, 32);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 32);
            card.setLayoutParams(params);
            card.setElevation(8f);
            card.setGravity(Gravity.CENTER_VERTICAL);

            // Product image
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setBackgroundResource(R.drawable.rounded_image_bg);
            String thumb = product.getThumbnail();
            if (thumb != null && !thumb.isEmpty()) {
                // Uncomment if you have Glide:
                // Glide.with(this).load(thumb).placeholder(android.R.drawable.ic_menu_gallery).into(imageView);
                imageView.setImageResource(android.R.drawable.ic_menu_gallery); // fallback
            } else {
                imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            // Info vertical
            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setPadding(24, 0, 0, 0);
            info.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            TextView title = new TextView(this);
            title.setText(product.getTitle());
            title.setTextSize(18);
            title.setTypeface(null, Typeface.BOLD);
            title.setTextColor(0xFF222222);
            title.setMaxLines(2);
            title.setEllipsize(android.text.TextUtils.TruncateAt.END);

            TextView price = new TextView(this);
            price.setText("$" + String.format("%.2f", product.getPrice()));
            price.setTextSize(16);
            price.setTextColor(0xFF4CAF50);
            price.setPadding(0, 8, 0, 0);

            info.addView(title);
            info.addView(price);

            // Heart button to remove from favorites
            ImageButton favButton = new ImageButton(this);
            favButton.setBackgroundColor(Color.TRANSPARENT);
            favButton.setScaleType(ImageButton.ScaleType.CENTER_INSIDE);
            favButton.setPadding(0, 0, 0, 0);
            LinearLayout.LayoutParams favParams = new LinearLayout.LayoutParams(80, 80);
            favParams.setMargins(0, 0, 0, 0);
            favButton.setLayoutParams(favParams);
            favButton.setImageResource(android.R.drawable.btn_star_big_on);
            favButton.setColorFilter(Color.RED);
            favButton.setContentDescription("Remove from favorites");
            favButton.setOnClickListener(v -> {
                SupabaseManager.removeFavorite(product.getId(), new SupabaseManager.AuthCallback() {
                    @Override
                    public void onSuccess(String msg) {
                        runOnUiThread(() -> {
                            Toast.makeText(FavoriteActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                            loadFavorites();
                        });
                    }
                    @Override
                    public void onError(String err) {
                        runOnUiThread(() -> Toast.makeText(FavoriteActivity.this, "Failed to remove favorite", Toast.LENGTH_SHORT).show());
                    }
                });
            });

            // Card click opens product detail
            card.setOnClickListener(v -> {
                Intent intent = new Intent(FavoriteActivity.this, ProductDetailActivity.class);
                intent.putExtra("product", product);
                startActivity(intent);
            });

            card.addView(imageView);
            card.addView(info);
            card.addView(favButton);
            favoritesContainer.addView(card);
        }
    }

    private void showEmptyMessage() {
        favoritesContainer.removeAllViews();
        TextView empty = new TextView(this);
        empty.setText("No favorite items yet.");
        empty.setTextSize(18);
        empty.setTextColor(0xFF888888);
        empty.setGravity(Gravity.CENTER);
        favoritesContainer.addView(empty);
    }
} 