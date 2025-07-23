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
import com.praneet.neo.repository.ProductRepository;
import com.praneet.neo.repository.ProductRepositoryImpl;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.content.Intent;
import android.widget.HorizontalScrollView;
import com.praneet.neo.SupabaseManager;
import com.praneet.neo.ProductDatabaseManager;
import android.widget.ImageButton;
import android.graphics.Color;
import android.widget.FrameLayout;
import com.praneet.neo.model.CartItem;
import com.google.firebase.analytics.FirebaseAnalytics;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity {
    private EditText searchInput;
    private LinearLayout categoryContainer;
    private LinearLayout productContainer;
    private ProductRepository productRepository;
    private ExecutorService executor;
    private List<Product> allProducts;
    private Set<String> availableCategories;
    // Store favorite product IDs for the current user
    private HashSet<Long> favoriteProductIds = new HashSet<>();
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Hide action bar if it exists
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        setContentView(R.layout.activity_main);
        
        // Initialize managers
        SupabaseManager.initialize(this);
        ProductDatabaseManager.initialize(this);
        
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        // Log a sample event for opening the home screen
        Bundle params = new Bundle();
        params.putString("screen_name", "HomeScreen");
        mFirebaseAnalytics.logEvent("screen_view", params);

        // Open cart page when cart button is clicked
        LinearLayout cartButton = findViewById(R.id.bottom_nav_cart);
        cartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });

        // Hide the old chat button
        Button chatButton = findViewById(R.id.chat_button);
        chatButton.setVisibility(View.GONE);

        // Floating chat bubble and overlay logic
        View fabChat = findViewById(R.id.fab_chat);
        View chatOverlay = findViewById(R.id.chat_overlay);
        ImageButton closeChat = findViewById(R.id.close_chat);
        WebView chatWebView = findViewById(R.id.chat_webview);

        fabChat.setOnClickListener(v -> {
            chatOverlay.setVisibility(View.VISIBLE);
            // Only load once per session for performance
            if (chatWebView.getUrl() == null) {
                WebSettings webSettings = chatWebView.getSettings();
                webSettings.setJavaScriptEnabled(true);
                webSettings.setDomStorageEnabled(true);
                webSettings.setLoadWithOverviewMode(true);
                webSettings.setUseWideViewPort(true);
                webSettings.setUserAgentString(
                    "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Mobile Safari/537.36");
                chatWebView.setWebViewClient(new WebViewClient());
                chatWebView.loadUrl("file:///android_asset/tawk.html");
            }
        });

        closeChat.setOnClickListener(v -> {
            chatOverlay.setVisibility(View.GONE);
        });


        // Responsive bottom navbar buttons
        findViewById(R.id.bottom_nav_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        setupViewAllButton();
        setupUserProfileSection();
        loadProducts();
        // Fetch favorites for the current user
        fetchFavoritesForCurrentUser();
    }

    private void initializeViews() {
        searchInput = findViewById(R.id.search_input);
        categoryContainer = findViewById(R.id.category_container);
        productContainer = findViewById(R.id.product_container);
    }
    
    private void setupUserProfileSection() {
        LinearLayout userProfileSection = findViewById(R.id.user_profile_section);
        TextView welcomeText = findViewById(R.id.welcome_text);
        TextView userNameText = findViewById(R.id.user_name_text);
        
        // Set click listener to go to account page
        userProfileSection.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AccountActivity.class);
            startActivity(intent);
        });
        
        // Check login status and update UI
        updateUserProfileUI();
    }
    
    private void updateUserProfileUI() {
        TextView welcomeText = findViewById(R.id.welcome_text);
        TextView userNameText = findViewById(R.id.user_name_text);
        // Check if user is logged in
        if (SupabaseManager.isSignedIn()) {
            welcomeText.setText("Welcome");
            String userName = SupabaseManager.getStoredUserName();
            if (userName != null && !userName.isEmpty()) {
                userNameText.setText(userName);
                android.util.Log.d("HomeUserName", "Setting home page user name: " + userName);
            } else {
                userNameText.setText("");
                android.util.Log.d("HomeUserName", "Setting home page user name: (empty)");
            }
            userNameText.setTextColor(getResources().getColor(android.R.color.black));
        } else {
            welcomeText.setText("Welcome");
            userNameText.setText("Login / Signup");
            userNameText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            android.util.Log.d("HomeUserName", "Setting home page user name: Login / Signup");
        }
    }

    private void setupRepository() {
        executor = Executors.newSingleThreadExecutor();
        productRepository = new ProductRepositoryImpl();
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

    private void setupViewAllButton() {
        TextView viewAllButton = findViewById(R.id.view_all_button);
        if (viewAllButton != null) {
            viewAllButton.setOnClickListener(v -> {
                // Show all products without any filtering
                if (allProducts != null) {
                    displayProducts(allProducts);
                    Toast.makeText(this, "📋 Showing all " + allProducts.size() + " products", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "❌ No products available", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadProducts() {
        executor.execute(() -> {
            try {
                System.out.println("📊 Loading products from database...");
                
                // Load products directly from database
                List<Product> products = productRepository.getProducts();
                System.out.println("✅ Database load successful, got " + (products != null ? products.size() : 0) + " products");
                allProducts = products;
                
                runOnUiThread(() -> {
                    if (products != null && !products.isEmpty()) {
                        createDynamicCategoryNavbar(products);
                        displayProducts(products);
                        Toast.makeText(MainActivity.this, 
                            "✅ Loaded " + products.size() + " products from database!", 
                            Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, 
                            "❌ No products found in database", 
                            Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                System.out.println("❌ Database operation failed: " + e.getMessage());
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, 
                        "❌ Error loading from database: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
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
            noProductsText.setText("No products found in database");
            noProductsText.setTextSize(16);
            noProductsText.setGravity(android.view.Gravity.CENTER);
            noProductsText.setPadding(32, 32, 32, 32);
            productContainer.addView(noProductsText);
            return;
        }

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
            } else {
                // Add invisible placeholder to keep layout consistent
                View placeholder = new View(this);
                LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                params2.setMargins(4, 8, 8, 8);
                placeholder.setLayoutParams(params2);
                placeholder.setVisibility(View.INVISIBLE);
                row.addView(placeholder);
            }
            
            gridContainer.addView(row);
        }
        
        productContainer.addView(gridContainer);
    }

    private View createProductView(Product product) {
        // Card container with overlay support
        FrameLayout cardFrame = new FrameLayout(this);
        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        cardFrame.setLayoutParams(frameParams);

        // Main card content
        LinearLayout cardContainer = new LinearLayout(this);
        cardContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        cardContainer.setOrientation(LinearLayout.VERTICAL);
        cardContainer.setBackgroundColor(getResources().getColor(android.R.color.white));
        cardContainer.setElevation(getResources().getDimension(R.dimen.card_elevation));
        cardContainer.setPadding(12, 12, 12, 12);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        int cardMargin = (int) getResources().getDimension(R.dimen.card_margin);
        cardParams.setMargins(cardMargin, cardMargin, cardMargin, cardMargin);
        cardContainer.setLayoutParams(cardParams);
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
        LinearLayout.LayoutParams priceParams = (LinearLayout.LayoutParams) priceView.getLayoutParams();
        priceParams.setMargins(0, 0, 0, 16);
        priceView.setLayoutParams(priceParams);

        // Add to Cart button (pill style)
        Button addToCartButton = new Button(this);
        addToCartButton.setText("Add to Cart");
        addToCartButton.setTextSize(16);
        addToCartButton.setTypeface(null, android.graphics.Typeface.BOLD);
        addToCartButton.setAllCaps(true);
        addToCartButton.setTextColor(getResources().getColor(android.R.color.white));
        addToCartButton.setBackgroundResource(R.drawable.button_green_rounded);
        addToCartButton.setPadding(0, 36, 0, 36);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.setMargins(0, 16, 0, 0);
        addToCartButton.setLayoutParams(btnParams);
        addToCartButton.setOnClickListener(v -> {
            if (!SupabaseManager.isSignedIn()) {
                Toast.makeText(this, "Please log in or sign up first.", Toast.LENGTH_SHORT).show();
                return;
            }
            SupabaseManager.addToCart(product, 1, new SupabaseManager.CartCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Added to cart: " + product.getTitle() + " (ID: " + product.getId() + ")", Toast.LENGTH_SHORT).show();
                    });
                }
                @Override
                public void onError(String err) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to add to cart", Toast.LENGTH_SHORT).show());
                }
            });
        });

        // Favorite (heart) button (top-right, overlay)
        FrameLayout.LayoutParams heartParams = new FrameLayout.LayoutParams(90, 90);
        heartParams.gravity = android.view.Gravity.TOP | android.view.Gravity.END;
        heartParams.setMargins(0, 16, 16, 0);
        LinearLayout heartCircle = new LinearLayout(this);
        heartCircle.setLayoutParams(heartParams);
        heartCircle.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
        heartCircle.setPadding(0, 0, 0, 0);
        heartCircle.setGravity(android.view.Gravity.CENTER);
        heartCircle.setElevation(8f);
        TextView heartIcon = new TextView(this);
        boolean isFav = favoriteProductIds.contains((long) product.getId());
        heartIcon.setText(isFav ? "❤️" : "🤍");
        heartIcon.setTextSize(32);
        heartIcon.setGravity(android.view.Gravity.CENTER);
        heartCircle.addView(heartIcon);
        heartCircle.setOnClickListener(v -> {
            if (!SupabaseManager.isSignedIn()) {
                Toast.makeText(MainActivity.this, "Please log in or sign up first.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (favoriteProductIds.contains((long) product.getId())) {
                SupabaseManager.removeFavorite(product.getId(), new SupabaseManager.AuthCallback() {
                    @Override
                    public void onSuccess(String msg) {
                        runOnUiThread(() -> {
                            favoriteProductIds.remove((long) product.getId());
                            heartIcon.setText("🤍");
                        });
                    }
                    @Override
                    public void onError(String err) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to remove favorite", Toast.LENGTH_SHORT).show());
                    }
                });
            } else {
                SupabaseManager.addFavorite(product.getId(), new SupabaseManager.AuthCallback() {
                    @Override
                    public void onSuccess(String msg) {
                        runOnUiThread(() -> {
                            favoriteProductIds.add((long) product.getId());
                            heartIcon.setText("❤️");
                        });
                    }
                    @Override
                    public void onError(String err) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to add favorite", Toast.LENGTH_SHORT).show());
                    }
                });
            }
        });

        // Add all views to details container
        detailsContainer.addView(titleView);
        detailsContainer.addView(descView);
        detailsContainer.addView(priceView);
        detailsContainer.addView(addToCartButton);

        // Add image and details to card
        cardContainer.addView(imageContainer);
        cardContainer.addView(detailsContainer);

        // Add cardContainer and heartCircle to FrameLayout
        cardFrame.addView(cardContainer);
        cardFrame.addView(heartCircle);

        return cardFrame;
    }

    private void addToCart(Product product) {
        addToCart(product, 1);
    }
    
    private void addToCart(Product product, int quantity) {
        SupabaseManager.addToCart(product, quantity, new SupabaseManager.CartCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    String message = quantity > 1 ? 
                        "🛒 " + quantity + "x " + product.getTitle() + " added to cart!" :
                        "🛒 " + product.getTitle() + " added to cart!";
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    // Update cart badge if you have one
                    updateCartBadge();
                });
            }
            @Override
            public void onError(String err) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to add to cart", Toast.LENGTH_SHORT).show());
            }
        });
    }
    
    private void showQuantityDialog(Product product) {
        // Create a simple dialog for quantity selection
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Add to Cart");
        builder.setMessage("How many " + product.getTitle() + " would you like to add?");
        
        // Create input field for quantity
        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setText("1");
        input.setSelection(input.getText().length());
        builder.setView(input);
        
        builder.setPositiveButton("Add", (dialog, which) -> {
            try {
                int quantity = Integer.parseInt(input.getText().toString());
                if (quantity > 0 && quantity <= 100) {
                    addToCart(product, quantity);
                } else {
                    Toast.makeText(this, "Please enter a valid quantity (1-100)", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        
        builder.show();
    }
    
    private void updateCartBadge() {
        SupabaseManager.getCartItems(new SupabaseManager.CartItemsCallback() {
            @Override
            public void onSuccess(List<CartItem> cartItems) {
                runOnUiThread(() -> {
                    // You can add a badge to the cart button here if needed
                    // For now, we'll just log the count
                    System.out.println("Cart now has " + cartItems.size() + " items");
                });
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    System.out.println("Error getting cart items: " + error);
                });
            }
        });
    }

    private void openProductDetail(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product", product);
        startActivity(intent);
    }

    private void fetchFavoritesForCurrentUser() {
        SupabaseManager.getFavorites(new SupabaseManager.FavoritesCallback() {
            @Override
            public void onSuccess(List<Long> productIds) {
                runOnUiThread(() -> {
                    favoriteProductIds.clear();
                    favoriteProductIds.addAll(productIds);
                    // Refresh product list UI if needed
                    displayProducts(allProducts);
                });
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    favoriteProductIds.clear();
                    displayProducts(allProducts);
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update user profile UI when returning from account page
        updateUserProfileUI();
        // Refresh favorites in case user switched accounts
        fetchFavoritesForCurrentUser();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
} 