package com.praneet.neo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.praneet.neo.model.Product;

public class ProductDetailActivity extends AppCompatActivity {
    private Product product;
    private int quantity = 1;
    
    private TextView productTitle, productBrand, productDescription, productRating;
    private TextView productCategory, productStock, productDiscount, productPrice;
    private TextView quantityText;
    private Button decreaseQuantity, increaseQuantity, addToCartButton;
    private TextView backButton, favoriteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Hide action bar if it exists
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        setContentView(R.layout.activity_product_detail);
        
        // Get product data from intent
        Intent intent = getIntent();
        if (intent != null) {
            product = (Product) intent.getSerializableExtra("product");
        }
        
        if (product == null) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initializeViews();
        setupClickListeners();
        displayProductDetails();
    }

    private void initializeViews() {
        productTitle = findViewById(R.id.product_title);
        productBrand = findViewById(R.id.product_brand);
        productDescription = findViewById(R.id.product_description);
        productRating = findViewById(R.id.product_rating);
        productCategory = findViewById(R.id.product_category);
        productStock = findViewById(R.id.product_stock);
        productDiscount = findViewById(R.id.product_discount);
        productPrice = findViewById(R.id.product_price);
        quantityText = findViewById(R.id.quantity_text);
        decreaseQuantity = findViewById(R.id.decrease_quantity);
        increaseQuantity = findViewById(R.id.increase_quantity);
        addToCartButton = findViewById(R.id.add_to_cart_button);
        backButton = findViewById(R.id.back_button);
        favoriteButton = findViewById(R.id.favorite_button);
    }

    private void setupClickListeners() {
        // Back button
        backButton.setOnClickListener(v -> finish());
        
        // Favorite button
        favoriteButton.setOnClickListener(v -> {
            favoriteButton.setText("❤️");
            Toast.makeText(this, "Added to favorites!", Toast.LENGTH_SHORT).show();
        });
        
        // Quantity controls
        decreaseQuantity.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                quantityText.setText(String.valueOf(quantity));
                updatePrice();
            }
        });
        
        increaseQuantity.setOnClickListener(v -> {
            quantity++;
            quantityText.setText(String.valueOf(quantity));
            updatePrice();
        });
        
        // Add to cart button
        addToCartButton.setOnClickListener(v -> {
            addToCart();
        });
    }

    private void displayProductDetails() {
        // Set product information
        productTitle.setText(product.getTitle());
        productBrand.setText("by " + product.getBrand());
        productDescription.setText(product.getDescription());
        productRating.setText(String.valueOf(product.getRating()));
        productCategory.setText(product.getCategory());
        productPrice.setText("$ " + product.getPrice());
        
        // Set stock status
        if (product.getStock() > 0) {
            productStock.setText("In Stock (" + product.getStock() + " available)");
            productStock.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            productStock.setText("Out of Stock");
            productStock.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            addToCartButton.setEnabled(false);
            addToCartButton.setText("Out of Stock");
        }
        
        // Set discount
        if (product.getDiscountPercentage() > 0) {
            productDiscount.setText(product.getDiscountPercentage() + "% OFF");
            productDiscount.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            productDiscount.setText("No discount");
            productDiscount.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
        
        // Set initial quantity and price
        quantityText.setText("1");
        updatePrice();
    }

    private void updatePrice() {
        double totalPrice = product.getPrice() * quantity;
        productPrice.setText("$ " + String.format("%.2f", totalPrice));
    }

    private void addToCart() {
        double totalPrice = product.getPrice() * quantity;
        String message = quantity + "x " + product.getTitle() + " added to cart for $" + String.format("%.2f", totalPrice);
        
        Toast.makeText(this, "🛒 " + message, Toast.LENGTH_LONG).show();
        
        // Here you would typically add to a cart manager or database
        // For now, we'll just show a success message
        
        // Optionally go back to main activity
        // finish();
    }
} 