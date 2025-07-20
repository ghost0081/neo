package com.praneet.neo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.praneet.neo.model.Product;
import java.util.List;

public class DatabaseTestActivity extends AppCompatActivity {
    private static final String TAG = "DatabaseTest";
    private TextView logText;
    private Button testDummyApiButton;
    private Button testSupabaseButton;
    private Button testSyncButton;
    private Button clearLogButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_test);
        
        // Initialize managers
        SupabaseManager.initialize(this);
        ProductDatabaseManager.initialize(this);
        
        // Initialize views
        logText = findViewById(R.id.logText);
        testDummyApiButton = findViewById(R.id.testDummyApiButton);
        testSupabaseButton = findViewById(R.id.testSupabaseButton);
        testSyncButton = findViewById(R.id.testSyncButton);
        clearLogButton = findViewById(R.id.clearLogButton);
        
        // Set up button click listeners
        setupButtonListeners();
        
        log("Database Test Activity initialized");
    }
    
    private void setupButtonListeners() {
        testDummyApiButton.setOnClickListener(v -> testSupabaseConnection());
        testSupabaseButton.setOnClickListener(v -> testSupabaseConnection());
        testSyncButton.setOnClickListener(v -> testSupabaseConnection());
        clearLogButton.setOnClickListener(v -> clearLog());
    }
    
    private void testSupabaseConnection() {
        log("Testing Supabase connection...");
        testSupabaseButton.setEnabled(false);
        
        ProductDatabaseManager.fetchProductsFromSupabase(new ProductDatabaseManager.ProductCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                log("✅ Supabase connection successful!");
                log("📊 Found " + products.size() + " products in database");
                if (products.size() > 0) {
                    Product firstProduct = products.get(0);
                    log("📦 Sample product: " + firstProduct.getTitle());
                    log("💰 Price: $" + firstProduct.getPrice());
                } else {
                    log("📭 Database is empty - ready for sync");
                }
                runOnUiThread(() -> testSupabaseButton.setEnabled(true));
            }
            
            @Override
            public void onError(String error) {
                log("❌ Supabase connection failed: " + error);
                log("💡 Make sure you've run the SQL script in your Supabase dashboard");
                runOnUiThread(() -> {
                    testSupabaseButton.setEnabled(true);
                    Toast.makeText(DatabaseTestActivity.this, "Supabase test failed", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    

    
    private void clearLog() {
        logText.setText("");
        log("Log cleared");
    }
    
    private void log(String message) {
        String timestamp = java.time.LocalTime.now().toString().substring(0, 8);
        String logMessage = "[" + timestamp + "] " + message + "\n";
        
        runOnUiThread(() -> {
            logText.append(logMessage);
            // Auto-scroll to bottom
            final int scrollAmount = logText.getLayout().getLineTop(logText.getLineCount()) - logText.getHeight();
            if (scrollAmount > 0) {
                logText.scrollTo(0, scrollAmount);
            }
        });
        
        Log.d(TAG, message);
    }
} 