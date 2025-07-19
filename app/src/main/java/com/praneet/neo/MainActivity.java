package com.praneet.neo;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.praneet.neo.model.Product;
import com.praneet.neo.network.NetworkManager;
import com.praneet.neo.network.ProductApiService;
import com.praneet.neo.repository.ProductRepository;
import com.praneet.neo.repository.ProductRepositoryImpl;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private EditText searchInput;
    private LinearLayout categoryContainer;
    private LinearLayout productContainer;
    private ProductRepository productRepository;
    private ExecutorService executor;
    private List<Product> allProducts;
    private TextView statusText;
    private Set<String> availableCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeViews();
        setupRepository();
        setupSearchFunctionality();
        loadProducts();
    }

    private void initializeViews() {
        searchInput = findViewById(R.id.search_input);
        categoryContainer = findViewById(R.id.category_container);
        productContainer = findViewById(R.id.product_container);
        
        // Add status text for API feedback
        statusText = new TextView(this);
        statusText.setText("Loading products from DummyJSON API...");
        statusText.setTextSize(14);
        statusText.setGravity(android.view.Gravity.CENTER);
        statusText.setPadding(32, 16, 32, 16);
        statusText.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        productContainer.addView(statusText);
    }

    private void setupRepository() {
        executor = Executors.newSingleThreadExecutor();
        ProductApiService apiService = NetworkManager.getInstance().getProductApiService();
        productRepository = new ProductRepositoryImpl(apiService);
    }

    private void setupSearchFunctionality() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = s.toString().toLowerCase();
                if (searchText.length() > 2) {
                    searchProducts(searchText);
                } else if (searchText.isEmpty()) {
                    displayProducts(allProducts);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadProducts() {
        executor.execute(() -> {
            try {
                System.out.println("🔍 Starting API call to DummyJSON...");
                List<Product> products = productRepository.getProducts();
                System.out.println("✅ API call successful, got " + (products != null ? products.size() : 0) + " products");
                allProducts = products;
                runOnUiThread(() -> {
                    if (products != null && !products.isEmpty()) {
                        createDynamicCategoryNavbar(products);
                        displayProducts(products);
                        Toast.makeText(MainActivity.this, 
                            "✅ Loaded " + products.size() + " products from DummyJSON API!", 
                            Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, 
                            "❌ No products loaded from API", 
                            Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                System.out.println("❌ API call failed: " + e.getMessage());
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, 
                        "❌ Error loading from DummyJSON API: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                    statusText.setText("❌ Failed to load from DummyJSON API: " + e.getMessage());
                    statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                });
            }
        });
    }

    private void createDynamicCategoryNavbar(List<Product> products) {
        // Clear existing category buttons
        categoryContainer.removeAllViews();
        
        // Extract unique categories from products
        availableCategories = new HashSet<>();
        for (Product product : products) {
            if (product.getCategory() != null && !product.getCategory().isEmpty()) {
                availableCategories.add(product.getCategory());
            }
        }
        
        // Create "ALL" button first
        Button allButton = createCategoryButton("ALL", true);
        allButton.setOnClickListener(v -> {
            selectCategoryButton(allButton);
            displayProducts(allProducts);
        });
        categoryContainer.addView(allButton);
        
        // Create category buttons dynamically
        for (String category : availableCategories) {
            Button categoryButton = createCategoryButton(category, false);
            categoryButton.setOnClickListener(v -> {
                selectCategoryButton(categoryButton);
                filterByCategory(category);
            });
            categoryContainer.addView(categoryButton);
        }
    }

    private Button createCategoryButton(String text, boolean isSelected) {
        Button button = new Button(this);
        button.setText(text.toUpperCase());
        button.setTextSize(12);
        button.setPadding(32, 16, 32, 16);
        button.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        
        if (isSelected) {
            button.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
            button.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            button.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            button.setTextColor(getResources().getColor(android.R.color.black));
        }
        
        return button;
    }

    private void selectCategoryButton(Button selectedButton) {
        // Reset all buttons
        for (int i = 0; i < categoryContainer.getChildCount(); i++) {
            View child = categoryContainer.getChildAt(i);
            if (child instanceof Button) {
                Button button = (Button) child;
                button.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                button.setTextColor(getResources().getColor(android.R.color.black));
            }
        }
        
        // Select the clicked button
        selectedButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
        selectedButton.setTextColor(getResources().getColor(android.R.color.white));
    }

    private void searchProducts(String query) {
        executor.execute(() -> {
            try {
                List<Product> products = productRepository.searchProducts(query);
                runOnUiThread(() -> {
                    displayProducts(products);
                    Toast.makeText(MainActivity.this, 
                        "🔍 Found " + (products != null ? products.size() : 0) + " products for '" + query + "'", 
                        Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, 
                        "❌ Search error: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void filterByCategory(String category) {
        executor.execute(() -> {
            try {
                List<Product> products = productRepository.getProductsByCategory(category);
                runOnUiThread(() -> {
                    displayProducts(products);
                    Toast.makeText(MainActivity.this, 
                        "📂 Found " + (products != null ? products.size() : 0) + " products in " + category, 
                        Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, 
                        "❌ Category filter error: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void displayProducts(List<Product> products) {
        if (productContainer == null) return;
        
        // Remove status text and all existing products
        productContainer.removeAllViews();
        
        if (products == null || products.isEmpty()) {
            TextView noProductsText = new TextView(this);
            noProductsText.setText("No products found in DummyJSON API");
            noProductsText.setTextSize(16);
            noProductsText.setGravity(android.view.Gravity.CENTER);
            noProductsText.setPadding(32, 32, 32, 32);
            productContainer.addView(noProductsText);
            return;
        }

        // Add status showing API data
        TextView apiStatusText = new TextView(this);
        apiStatusText.setText("📡 DummyJSON API Data: " + products.size() + " products loaded");
        apiStatusText.setTextSize(12);
        apiStatusText.setGravity(android.view.Gravity.CENTER);
        apiStatusText.setPadding(16, 8, 16, 8);
        apiStatusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        productContainer.addView(apiStatusText);

        for (Product product : products) {
            View productView = createProductView(product);
            productContainer.addView(productView);
        }
    }

    private View createProductView(Product product) {
        LinearLayout productLayout = new LinearLayout(this);
        productLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        productLayout.setOrientation(LinearLayout.HORIZONTAL);
        productLayout.setBackgroundColor(getResources().getColor(android.R.color.white));
        productLayout.setPadding(64, 64, 64, 64);
        productLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        
        // Product image placeholder
        TextView imageView = new TextView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(320, 320));
        imageView.setText("🛍️");
        imageView.setTextSize(40);
        imageView.setGravity(android.view.Gravity.CENTER);
        imageView.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        imageView.setPadding(32, 32, 32, 32);
        
        // Product details
        LinearLayout detailsLayout = new LinearLayout(this);
        detailsLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        detailsLayout.setOrientation(LinearLayout.VERTICAL);
        detailsLayout.setPadding(32, 0, 0, 0);
        
        // Product title
        TextView titleView = new TextView(this);
        titleView.setText(product.getTitle());
        titleView.setTextSize(18);
        titleView.setTextColor(getResources().getColor(android.R.color.black));
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        
        // Product brand
        TextView brandView = new TextView(this);
        brandView.setText("by " + product.getBrand());
        brandView.setTextSize(14);
        brandView.setTextColor(getResources().getColor(android.R.color.darker_gray));
        
        // Product description
        TextView descView = new TextView(this);
        descView.setText(product.getDescription());
        descView.setTextSize(14);
        descView.setTextColor(getResources().getColor(android.R.color.black));
        descView.setMaxLines(2);
        descView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        
        // Product rating
        TextView ratingView = new TextView(this);
        ratingView.setText("⭐ Rating: " + product.getRating() + "/5");
        ratingView.setTextSize(12);
        ratingView.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        
        // Price and buy button
        LinearLayout priceLayout = new LinearLayout(this);
        priceLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        priceLayout.setOrientation(LinearLayout.HORIZONTAL);
        
        TextView priceView = new TextView(this);
        priceView.setText("$ " + product.getPrice());
        priceView.setTextSize(18);
        priceView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        priceView.setTypeface(null, android.graphics.Typeface.BOLD);
        
        Button buyButton = new Button(this);
        buyButton.setText("Buy");
        buyButton.setBackgroundColor(getResources().getColor(android.R.color.black));
        buyButton.setTextColor(getResources().getColor(android.R.color.white));
        buyButton.setOnClickListener(v -> addToCart(product.getTitle()));
        
        priceLayout.addView(priceView, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        priceLayout.addView(buyButton);
        
        // Add all views to details layout
        detailsLayout.addView(titleView);
        detailsLayout.addView(brandView);
        detailsLayout.addView(descView);
        detailsLayout.addView(ratingView);
        detailsLayout.addView(priceLayout);
        
        // Add to main product layout
        productLayout.addView(imageView);
        productLayout.addView(detailsLayout);
        
        return productLayout;
    }

    private void addToCart(String productName) {
        Toast.makeText(this, "🛒 " + productName + " added to cart!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
} 