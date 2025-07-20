package com.praneet.neo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.praneet.neo.repository.ProductRepository;
import com.praneet.neo.repository.ProductRepositoryImpl;
import com.praneet.neo.model.Product;
import java.util.List;

public class DatabaseSyncActivity extends AppCompatActivity {
    private ProductRepository productRepository;
    private TextView statusText;
    private Button syncButton;
    private Button fetchButton;
    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_sync);
        
        // Initialize managers
        SupabaseManager.initialize(this);
        ProductDatabaseManager.initialize(this);
        
        // Initialize repository
        productRepository = new ProductRepositoryImpl();
        
        // Initialize views
        statusText = findViewById(R.id.statusText);
        syncButton = findViewById(R.id.syncButton);
        fetchButton = findViewById(R.id.fetchButton);
        searchButton = findViewById(R.id.searchButton);
        
        // Set up button click listeners
        setupButtonListeners();
        
        updateStatus();
    }
    
    private void setupButtonListeners() {
        syncButton.setOnClickListener(v -> fetchProducts());
        fetchButton.setOnClickListener(v -> fetchProducts());
        searchButton.setOnClickListener(v -> searchProducts());
    }
    
    private void fetchProducts() {
        fetchButton.setEnabled(false);
        statusText.setText("Fetching products from database...");
        
        new Thread(() -> {
            try {
                List<Product> products = productRepository.getProducts();
                runOnUiThread(() -> {
                    statusText.setText("Fetched " + (products != null ? products.size() : 0) + " products from database");
                    fetchButton.setEnabled(true);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error fetching products: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    statusText.setText("Error: " + e.getMessage());
                    fetchButton.setEnabled(true);
                });
            }
        }).start();
    }
    
    private void searchProducts() {
        searchButton.setEnabled(false);
        statusText.setText("Searching products in database...");
        
        new Thread(() -> {
            try {
                List<Product> products = productRepository.searchProducts("phone");
                runOnUiThread(() -> {
                    statusText.setText("Found " + (products != null ? products.size() : 0) + " products matching 'phone' in database");
                    searchButton.setEnabled(true);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error searching products: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    statusText.setText("Error: " + e.getMessage());
                    searchButton.setEnabled(true);
                });
            }
        }).start();
    }
    
    private void updateStatus() {
        statusText.setText("Current data source: Supabase Database");
    }
} 