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
import android.content.Intent;
import android.widget.HorizontalScrollView;

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
        
        // Open cart page when cart button is clicked
        LinearLayout cartButton = findViewById(R.id.bottom_nav_cart);
        cartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });

        // Responsive bottom navbar buttons
        findViewById(R.id.bottom_nav_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Already on home, do nothing or refresh
                Toast.makeText(MainActivity.this, "Home clicked", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.bottom_nav_favorite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FavoriteActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.bottom_nav_order).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, OrderActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.bottom_nav_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                startActivity(intent);
            }
        });
        
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

        // Add header row: 'Categories' and 'See all >'
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        headerRow.setPadding(0, 0, 0, 0);
        // Add bottom margin to header row
        LinearLayout.LayoutParams headerParams = (LinearLayout.LayoutParams) headerRow.getLayoutParams();
        headerParams.setMargins(0, 0, 0, 8);
        headerRow.setLayoutParams(headerParams);

        TextView title = new TextView(this);
        title.setText("Categories");
        title.setTextSize(20);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextColor(getResources().getColor(android.R.color.black));
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f); // weight=1 for left alignment
        title.setLayoutParams(titleParams);

        TextView seeAll = new TextView(this);
        seeAll.setText("See all >");
        seeAll.setTextSize(15);
        seeAll.setTextColor(getResources().getColor(android.R.color.darker_gray));
        seeAll.setPadding(0, 0, 0, 0);
        seeAll.setOnClickListener(v -> Toast.makeText(this, "See all categories", Toast.LENGTH_SHORT).show());
        LinearLayout.LayoutParams seeAllParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        seeAll.setLayoutParams(seeAllParams);

        headerRow.addView(title);
        headerRow.addView(seeAll);
        categoryContainer.addView(headerRow);

        // Add vertical margin between heading and icons row
        View spacer = new View(this);
        LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 32); // 32px ~ 16dp
        spacer.setLayoutParams(spacerParams);
        categoryContainer.addView(spacer);

        // Now add the categories icon row BELOW the header row
        HorizontalScrollView scrollView = new HorizontalScrollView(this);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        scrollView.setHorizontalScrollBarEnabled(false);

        LinearLayout categoriesRow = new LinearLayout(this);
        categoriesRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        categoriesRow.setOrientation(LinearLayout.HORIZONTAL);
        categoriesRow.setPadding(0, 0, 0, 0);

        // Extract unique categories from products
        availableCategories = new HashSet<>();
        for (Product product : products) {
            if (product.getCategory() != null && !product.getCategory().isEmpty()) {
                availableCategories.add(product.getCategory());
            }
        }

        // Create category buttons dynamically (no selection highlight)
        for (String category : availableCategories) {
            LinearLayout categoryLayout = new LinearLayout(this);
            LinearLayout.LayoutParams catLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            catLayoutParams.setMargins(18, 0, 18, 0); // More even horizontal spacing
            categoryLayout.setLayoutParams(catLayoutParams);
            categoryLayout.setOrientation(LinearLayout.VERTICAL);
            categoryLayout.setGravity(android.view.Gravity.CENTER_HORIZONTAL);

            // Icon in lighter gray circle
            LinearLayout iconContainer = new LinearLayout(this);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(72, 72);
            iconContainer.setLayoutParams(iconParams);
            iconContainer.setGravity(android.view.Gravity.CENTER);
            iconContainer.setBackgroundResource(R.drawable.circle_background); // Use your light gray circle drawable
            iconContainer.setPadding(8, 8, 8, 8);
            iconContainer.setElevation(2f);

            TextView iconView = new TextView(this);
            iconView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            iconView.setGravity(android.view.Gravity.CENTER);
            iconView.setTextSize(28);
            iconView.setText(getCategoryIcon(category));

            iconContainer.addView(iconView);
            categoryLayout.addView(iconContainer);

            // Category name (bold, proper case)
            TextView nameView = new TextView(this);
            nameView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            String capName = category.substring(0, 1).toUpperCase() + category.substring(1).toLowerCase();
            nameView.setText(capName);
            nameView.setTextSize(14);
            nameView.setTypeface(null, android.graphics.Typeface.BOLD);
            nameView.setTextColor(getResources().getColor(android.R.color.black));
            nameView.setGravity(android.view.Gravity.CENTER);
            nameView.setPadding(0, 10, 0, 0);
            categoryLayout.addView(nameView);

            // Add click listener to filter by category
            categoryLayout.setClickable(true);
            categoryLayout.setFocusable(true);
            categoryLayout.setOnClickListener(v -> filterByCategory(category));

            categoriesRow.addView(categoryLayout);
        }

        // Add extra bottom margin to the categories row for separation
        LinearLayout.LayoutParams catRowParams = (LinearLayout.LayoutParams) categoriesRow.getLayoutParams();
        catRowParams.setMargins(0, 0, 0, 18);
        categoriesRow.setLayoutParams(catRowParams);

        scrollView.addView(categoriesRow);
        categoryContainer.addView(scrollView);
    }
    
    private String getCategoryIcon(String category) {
        switch (category.toLowerCase()) {
            case "all": return "🛍️";
            case "coffee & tea": return "☕";
            case "fruits": return "🍓";
            case "fast food": return "🍔";
            case "vegetables": return "🥬";
            case "dairy": return "🥛";
            case "meat": return "🥩";
            case "beverages": return "🥤";
            case "snacks": return "🍿";
            case "bakery": return "🥖";
            default: return "🛍️";
        }
    }

    private void selectCategoryButton(View selectedButton) {
        // Reset all category buttons
        for (int i = 0; i < categoryContainer.getChildCount(); i++) {
            View child = categoryContainer.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout categoryLayout = (LinearLayout) child;
                // Reset icon container background
                if (categoryLayout instanceof ViewGroup && ((ViewGroup) categoryLayout).getChildCount() > 0) {
                    View iconContainer = ((ViewGroup) categoryLayout).getChildAt(0);
                    if (iconContainer instanceof LinearLayout) {
                        iconContainer.setBackgroundResource(R.drawable.circle_background);
                        iconContainer.setElevation(2f);
                        // Reset icon color
                        if (iconContainer instanceof ViewGroup && ((ViewGroup) iconContainer).getChildCount() > 0) {
                            View iconView = ((ViewGroup) iconContainer).getChildAt(0);
                            if (iconView instanceof TextView) {
                                ((TextView) iconView).setTextColor(getResources().getColor(android.R.color.holo_blue_light));
                            }
                        }
                    }
                }
                // Reset text color and style
                if (categoryLayout instanceof ViewGroup && ((ViewGroup) categoryLayout).getChildCount() > 1) {
                    View nameView = ((ViewGroup) categoryLayout).getChildAt(1);
                    if (nameView instanceof TextView) {
                        ((TextView) nameView).setTextColor(getResources().getColor(android.R.color.darker_gray));
                        ((TextView) nameView).setTypeface(null, android.graphics.Typeface.NORMAL);
                    }
                }
            }
        }
        
        // Select the clicked button
        if (selectedButton instanceof LinearLayout) {
            LinearLayout categoryLayout = (LinearLayout) selectedButton;
            // Set icon container background
            if (categoryLayout instanceof ViewGroup && ((ViewGroup) categoryLayout).getChildCount() > 0) {
                View iconContainer = ((ViewGroup) categoryLayout).getChildAt(0);
                if (iconContainer instanceof LinearLayout) {
                    iconContainer.setBackgroundResource(R.drawable.circle_background_green);
                    iconContainer.setElevation(0f);
                    // Set icon color
                    if (iconContainer instanceof ViewGroup && ((ViewGroup) iconContainer).getChildCount() > 0) {
                        View iconView = ((ViewGroup) iconContainer).getChildAt(0);
                        if (iconView instanceof TextView) {
                            ((TextView) iconView).setTextColor(getResources().getColor(android.R.color.white));
                        }
                    }
                }
            }
            // Set text color and style
            if (categoryLayout instanceof ViewGroup && ((ViewGroup) categoryLayout).getChildCount() > 1) {
                View nameView = ((ViewGroup) categoryLayout).getChildAt(1);
                if (nameView instanceof TextView) {
                    ((TextView) nameView).setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    ((TextView) nameView).setTypeface(null, android.graphics.Typeface.BOLD);
                }
            }
        }
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

        // Create grid layout for products
        LinearLayout gridContainer = new LinearLayout(this);
        gridContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        gridContainer.setOrientation(LinearLayout.VERTICAL);
        
        // Add products in rows of 2
        for (int i = 0; i < products.size(); i += 2) {
            LinearLayout row = new LinearLayout(this);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            row.setOrientation(LinearLayout.HORIZONTAL);
            
            // First product in row
            View productView1 = createProductView(products.get(i));
            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            params1.setMargins(8, 8, 4, 8);
            productView1.setLayoutParams(params1);
            row.addView(productView1);
            
            // Second product in row (if exists)
            if (i + 1 < products.size()) {
                View productView2 = createProductView(products.get(i + 1));
                LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                params2.setMargins(4, 8, 8, 8);
                productView2.setLayoutParams(params2);
                row.addView(productView2);
            }
            
            gridContainer.addView(row);
        }
        
        productContainer.addView(gridContainer);
    }

    private View createProductView(Product product) {
        // Product card container (grid style)
        LinearLayout cardContainer = new LinearLayout(this);
        cardContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        cardContainer.setOrientation(LinearLayout.VERTICAL);
        cardContainer.setBackgroundColor(getResources().getColor(android.R.color.white));
        cardContainer.setElevation(getResources().getDimension(R.dimen.card_elevation));
        cardContainer.setPadding(12, 12, 12, 12);
        
        // Add margin between cards
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        int cardMargin = (int) getResources().getDimension(R.dimen.card_margin);
        cardParams.setMargins(cardMargin, cardMargin, cardMargin, cardMargin);
        cardContainer.setLayoutParams(cardParams);
        
        // Make the entire card clickable
        cardContainer.setClickable(true);
        cardContainer.setFocusable(true);
        cardContainer.setBackgroundResource(android.R.drawable.list_selector_background);
        cardContainer.setOnClickListener(v -> openProductDetail(product));
        
        // Product image container
        LinearLayout imageContainer = new LinearLayout(this);
        imageContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                120));
        imageContainer.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        imageContainer.setGravity(android.view.Gravity.CENTER);
        imageContainer.setPadding(16, 16, 16, 16);
        
        // Product image placeholder
        TextView imageView = new TextView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(60, 60));
        imageView.setText("🛍️");
        imageView.setTextSize(24);
        imageView.setGravity(android.view.Gravity.CENTER);
        imageView.setBackgroundColor(getResources().getColor(android.R.color.white));
        imageContainer.addView(imageView);
        
        // Product details container
        LinearLayout detailsContainer = new LinearLayout(this);
        detailsContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        detailsContainer.setOrientation(LinearLayout.VERTICAL);
        detailsContainer.setPadding(0, 12, 0, 0);
        
        // Product title
        TextView titleView = new TextView(this);
        titleView.setText(product.getTitle());
        titleView.setTextSize(14);
        titleView.setTextColor(getResources().getColor(android.R.color.black));
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setMaxLines(2);
        titleView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        
        // Product description
        TextView descView = new TextView(this);
        descView.setText("Original fresh " + product.getTitle().toLowerCase());
        descView.setTextSize(10);
        descView.setTextColor(getResources().getColor(android.R.color.darker_gray));
        descView.setMaxLines(1);
        descView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        
        // Price
        TextView priceView = new TextView(this);
        priceView.setText("$" + String.format("%.2f", product.getPrice()) + "/Kg");
        priceView.setTextSize(12);
        priceView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        priceView.setTypeface(null, android.graphics.Typeface.BOLD);
        priceView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        
        // Add to cart button (circular)
        LinearLayout buttonContainer = new LinearLayout(this);
        buttonContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
        buttonContainer.setGravity(android.view.Gravity.END);
        buttonContainer.setPadding(0, 8, 0, 0);
        
        LinearLayout cartButton = new LinearLayout(this);
        cartButton.setLayoutParams(new LinearLayout.LayoutParams(40, 40));
        cartButton.setBackgroundColor(getResources().getColor(android.R.color.black));
        cartButton.setGravity(android.view.Gravity.CENTER);
        cartButton.setElevation(2f);
        
        TextView cartIcon = new TextView(this);
        cartIcon.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        cartIcon.setText("🛒");
        cartIcon.setTextSize(16);
        cartIcon.setTextColor(getResources().getColor(android.R.color.white));
        
        cartButton.addView(cartIcon);
        cartButton.setOnClickListener(v -> {
            addToCart(product.getTitle());
        });
        
        buttonContainer.addView(cartButton);
        
        // Add all views to details container
        detailsContainer.addView(titleView);
        detailsContainer.addView(descView);
        detailsContainer.addView(priceView);
        detailsContainer.addView(buttonContainer);
        
        // Add to main card container
        cardContainer.addView(imageContainer);
        cardContainer.addView(detailsContainer);
        
        return cardContainer;
    }

    private void addToCart(String productName) {
        Toast.makeText(this, "🛒 " + productName + " added to cart!", Toast.LENGTH_SHORT).show();
    }

    private void openProductDetail(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product", product);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
} 