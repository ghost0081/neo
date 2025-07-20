package com.praneet.neo;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.view.Gravity;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.praneet.neo.model.Product;
import com.praneet.neo.repository.ProductRepository;
import com.praneet.neo.repository.ProductRepositoryImpl;
import java.util.List;
import java.util.ArrayList;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.util.Log;

public class AdminProductActivity extends AppCompatActivity {
    private LinearLayout productsContainer;
    private ProductRepository productRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(createLayout());
        productRepository = new ProductRepositoryImpl();
        loadProducts();
    }

    private View createLayout() {
        ScrollView scrollView = new ScrollView(this);
        productsContainer = new LinearLayout(this);
        productsContainer.setOrientation(LinearLayout.VERTICAL);
        productsContainer.setPadding(32, 32, 32, 32);
        scrollView.addView(productsContainer);

        // Add Product button
        Button addBtn = new Button(this);
        addBtn.setText("Add Product");
        addBtn.setAllCaps(true);
        addBtn.setTextSize(16);
        addBtn.setTypeface(null, Typeface.BOLD);
        addBtn.setBackgroundResource(R.drawable.button_green_rounded);
        addBtn.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnParams.setMargins(0, 0, 0, 32);
        addBtn.setLayoutParams(btnParams);
        addBtn.setPadding(0, 36, 0, 36);
        addBtn.setOnClickListener(v -> showAddProductDialog());
        productsContainer.addView(addBtn);

        return scrollView;
    }

    private void loadProducts() {
        new Thread(() -> {
            try {
                List<Product> products = productRepository.getProducts();
                runOnUiThread(() -> showProducts(products));
            } catch (Exception e) {
                runOnUiThread(() -> showEmptyMessage());
            }
        }).start();
    }

    private void showProducts(List<Product> products) {
        // Remove all except the Add button
        while (productsContainer.getChildCount() > 1) productsContainer.removeViewAt(1);
        if (products.isEmpty()) {
            showEmptyMessage();
            return;
        }
        for (Product product : products) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.HORIZONTAL);
            card.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
            card.setPadding(32, 32, 32, 32);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 24);
            card.setLayoutParams(params);
            card.setElevation(8f);
            card.setGravity(Gravity.CENTER_VERTICAL);

            // Product info vertical
            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            TextView name = new TextView(this);
            name.setText(product.getTitle());
            name.setTextSize(16);
            name.setTypeface(null, Typeface.BOLD);
            name.setTextColor(0xFF222222);
            name.setMaxLines(1);
            name.setEllipsize(android.text.TextUtils.TruncateAt.END);

            TextView price = new TextView(this);
            price.setText("$" + String.format("%.2f", product.getPrice()));
            price.setTextSize(15);
            price.setTextColor(0xFF4CAF50);
            price.setPadding(0, 8, 0, 0);

            info.addView(name);
            info.addView(price);

            // Delete button (red circle with trash emoji)
            LinearLayout delCircle = new LinearLayout(this);
            delCircle.setLayoutParams(new LinearLayout.LayoutParams(90, 90));
            delCircle.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
            delCircle.setPadding(0, 0, 0, 0);
            delCircle.setGravity(Gravity.CENTER);
            delCircle.setElevation(8f);
            delCircle.setBackgroundColor(0xFFD32F2F);
            TextView delIcon = new TextView(this);
            delIcon.setText("🗑️");
            delIcon.setTextSize(28);
            delIcon.setGravity(Gravity.CENTER);
            delIcon.setTextColor(0xFFFFFFFF);
            delCircle.addView(delIcon);
            delCircle.setOnClickListener(v -> deleteProduct(product.getId()));

            card.addView(info);
            card.addView(delCircle);
            productsContainer.addView(card);
        }
    }

    private void showEmptyMessage() {
        while (productsContainer.getChildCount() > 1) productsContainer.removeViewAt(1);
        TextView empty = new TextView(this);
        empty.setText("No products found.\nTap 'Add Product' to create one!");
        empty.setTextSize(18);
        empty.setTextColor(0xFF888888);
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(0, 64, 0, 0);
        productsContainer.addView(empty);
    }

    private void showAddProductDialog() {
        // Fetch categories from backend first
        new Thread(() -> {
            List<String> categories = fetchCategoriesFromBackend();
            runOnUiThread(() -> showAddProductDialogWithCategories(categories));
        }).start();
    }

    private List<String> fetchCategoriesFromBackend() {
        try {
            List<Product> products = productRepository.getProducts();
            java.util.Set<String> categorySet = new java.util.HashSet<>();
            for (Product p : products) {
                if (p.getCategory() != null && !p.getCategory().isEmpty()) {
                    categorySet.add(p.getCategory());
                }
            }
            List<String> categories = new ArrayList<>(categorySet);
            java.util.Collections.sort(categories);
            return categories;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void showAddProductDialogWithCategories(List<String> categories) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Add Product");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        final EditText titleInput = new EditText(this);
        titleInput.setHint("Product Title");
        final EditText descInput = new EditText(this);
        descInput.setHint("Description");
        final EditText priceInput = new EditText(this);
        priceInput.setHint("Price");
        priceInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        final EditText discountInput = new EditText(this);
        discountInput.setHint("Discount %");
        discountInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        final EditText ratingInput = new EditText(this);
        ratingInput.setHint("Rating");
        ratingInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        final EditText stockInput = new EditText(this);
        stockInput.setHint("Stock");
        stockInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        final EditText brandInput = new EditText(this);
        brandInput.setHint("Brand");
        final Spinner categorySpinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
        final EditText thumbInput = new EditText(this);
        thumbInput.setHint("Thumbnail URL");
        final EditText imagesInput = new EditText(this);
        imagesInput.setHint("Images (comma separated URLs)");
        layout.addView(titleInput);
        layout.addView(descInput);
        layout.addView(priceInput);
        layout.addView(discountInput);
        layout.addView(ratingInput);
        layout.addView(stockInput);
        layout.addView(brandInput);
        layout.addView(categorySpinner);
        layout.addView(thumbInput);
        layout.addView(imagesInput);
        builder.setView(layout);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = titleInput.getText().toString().trim();
            String desc = descInput.getText().toString().trim();
            String priceStr = priceInput.getText().toString().trim();
            String discountStr = discountInput.getText().toString().trim();
            String ratingStr = ratingInput.getText().toString().trim();
            String stockStr = stockInput.getText().toString().trim();
            String brand = brandInput.getText().toString().trim();
            String category = (String) categorySpinner.getSelectedItem();
            String thumb = thumbInput.getText().toString().trim();
            String imagesStr = imagesInput.getText().toString().trim();
            if (title.isEmpty() || desc.isEmpty() || priceStr.isEmpty() || discountStr.isEmpty() || ratingStr.isEmpty() || stockStr.isEmpty() || brand.isEmpty() || category == null || category.isEmpty() || thumb.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            double price = Double.parseDouble(priceStr);
            double discount = Double.parseDouble(discountStr);
            double rating = Double.parseDouble(ratingStr);
            int stock = Integer.parseInt(stockStr);
            List<String> images = new ArrayList<>();
            if (!imagesStr.isEmpty()) {
                for (String url : imagesStr.split(",")) {
                    if (!url.trim().isEmpty()) images.add(url.trim());
                }
            }
            addProduct(title, desc, price, discount, rating, stock, brand, category, thumb, images);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void addProduct(String title, String desc, double price, double discount, double rating, int stock, String brand, String category, String thumb, List<String> images) {
        new Thread(() -> {
            try {
                Product product = new Product();
                product.setTitle(title);
                product.setDescription(desc);
                product.setPrice(price);
                product.setDiscountPercentage(discount);
                product.setRating(rating);
                product.setStock(stock);
                product.setBrand(brand);
                product.setCategory(category);
                product.setThumbnail(thumb);
                product.setImages(images);
                ProductDatabaseManager.insertProduct(product, new ProductDatabaseManager.DatabaseCallback() {
                    @Override
                    public void onSuccess(String msg) {
                        runOnUiThread(() -> {
                            Toast.makeText(AdminProductActivity.this, "Product added", Toast.LENGTH_SHORT).show();
                            loadProducts();
                        });
                    }
                    @Override
                    public void onError(String error) {
                        Log.e("AdminProduct", "Add failed: " + error);
                        runOnUiThread(() -> Toast.makeText(AdminProductActivity.this, "Add failed: " + error, Toast.LENGTH_LONG).show());
                    }
                });
            } catch (Exception e) {
                Log.e("AdminProduct", "Add failed: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(AdminProductActivity.this, "Add failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void deleteProduct(int productId) {
        new Thread(() -> {
            try {
                ProductDatabaseManager.deleteProduct(productId, new ProductDatabaseManager.DatabaseCallback() {
                    @Override
                    public void onSuccess(String msg) {
                        runOnUiThread(() -> {
                            Toast.makeText(AdminProductActivity.this, "Product deleted", Toast.LENGTH_SHORT).show();
                            loadProducts();
                        });
                    }
                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> Toast.makeText(AdminProductActivity.this, "Delete failed: " + error, Toast.LENGTH_SHORT).show());
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(AdminProductActivity.this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
} 