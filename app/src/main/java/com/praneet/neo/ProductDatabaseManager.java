package com.praneet.neo;

import android.content.Context;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.*;
import com.praneet.neo.model.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ProductDatabaseManager {
    private static final String TAG = "ProductDatabaseManager";
    private static final String SUPABASE_URL = "https://xqofxqnwohkpbdtpotya.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inhxb2Z4cW53b2hrcGJkdHBvdHlhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTI5NDU0OTEsImV4cCI6MjA2ODUyMTQ5MX0.KZIo2Zuq3cxBZOliRrerPCQP7OeoRrehbXMxR4o6gIk";
    
    private static OkHttpClient httpClient;
    private static Context context;
    private static Gson gson;
    
    public static void initialize(Context appContext) {
        context = appContext.getApplicationContext();
        if (httpClient == null) {
            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();
        }
        if (gson == null) {
            gson = new Gson();
        }
    }
    

    
    // Store products in Supabase database
    public static void storeProductsInSupabase(List<Product> products, DatabaseCallback callback) {
        new Thread(() -> {
            try {
                // Check if initialized
                if (httpClient == null) {
                    callback.onError("ProductDatabaseManager not initialized. Call initialize() first.");
                    return;
                }
                
                String accessToken = SupabaseManager.getStoredAccessToken();
                if (accessToken == null || accessToken.isEmpty()) {
                    // Use anonymous access for database operations
                    accessToken = "";
                }
                
                // Make accessToken effectively final for lambda
                final String finalAccessToken = accessToken;
                
                // First, clear existing products
                clearAllProducts(finalAccessToken, () -> {
                    // Then insert new products
                    insertProductsBatch(products, finalAccessToken, callback);
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error storing products in Supabase: " + e.getMessage());
                callback.onError("Error storing products: " + e.getMessage());
            }
        }).start();
    }
    
    // Clear all products from database
    private static void clearAllProducts(String accessToken, Runnable onComplete) {
        try {
            Request.Builder requestBuilder = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/products")
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Content-Type", "application/json");
            
            if (!accessToken.isEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
            }
            
            Request request = requestBuilder.delete().build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                // Don't check response code as table might be empty
                onComplete.run();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clearing products: " + e.getMessage());
            onComplete.run(); // Continue anyway
        }
    }
    
    // Insert products in batch
    private static void insertProductsBatch(List<Product> products, String accessToken, DatabaseCallback callback) {
        try {
            Log.d(TAG, "Preparing to insert " + products.size() + " products to Supabase");
            
            JSONArray productsArray = new JSONArray();
            for (Product product : products) {
                try {
                    JSONObject productJson = new JSONObject();
                    productJson.put("id", product.getId());
                    productJson.put("title", product.getTitle() != null ? product.getTitle() : "");
                    productJson.put("description", product.getDescription() != null ? product.getDescription() : "");
                    productJson.put("price", product.getPrice());
                    productJson.put("discount_percentage", product.getDiscountPercentage());
                    productJson.put("rating", product.getRating());
                    productJson.put("stock", product.getStock());
                    productJson.put("brand", product.getBrand() != null ? product.getBrand() : "");
                    productJson.put("category", product.getCategory() != null ? product.getCategory() : "");
                    productJson.put("thumbnail", product.getThumbnail() != null ? product.getThumbnail() : "");
                    
                    // Handle images array safely
                    if (product.getImages() != null && !product.getImages().isEmpty()) {
                        JSONArray imagesArray = new JSONArray();
                        for (String image : product.getImages()) {
                            if (image != null && !image.isEmpty()) {
                                imagesArray.put(image);
                            }
                        }
                        productJson.put("images", imagesArray);
                    } else {
                        productJson.put("images", new JSONArray());
                    }
                    
                    productsArray.put(productJson);
                } catch (Exception e) {
                    Log.e(TAG, "Error preparing product " + product.getId() + ": " + e.getMessage());
                    // Continue with next product
                }
            }
            
            Log.d(TAG, "Prepared " + productsArray.length() + " products for insertion");
            
            RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), 
                productsArray.toString()
            );
            
            Request.Builder requestBuilder = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/products")
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .post(body);
            
            if (!accessToken.isEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
            }
            
            Request request = requestBuilder.build();
            
            Log.d(TAG, "Sending request to Supabase: " + request.url());
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Successfully inserted " + products.size() + " products");
                    callback.onSuccess("Successfully stored " + products.size() + " products in database");
                } else {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    Log.e(TAG, "Failed to store products: " + response.code() + " - " + responseBody);
                    callback.onError("Failed to store products: " + response.code() + " - " + responseBody);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error inserting products: " + e.getMessage());
            callback.onError("Error inserting products: " + e.getMessage());
        }
    }
    
    // Fetch products from Supabase database
    public static void fetchProductsFromSupabase(ProductCallback callback) {
        new Thread(() -> {
            try {
                // Check if initialized
                if (httpClient == null) {
                    callback.onError("ProductDatabaseManager not initialized. Call initialize() first.");
                    return;
                }
                
                String accessToken = SupabaseManager.getStoredAccessToken();
                if (accessToken == null || accessToken.isEmpty()) {
                    accessToken = "";
                }
                
                Log.d(TAG, "Fetching products from Supabase...");
                
                Request.Builder requestBuilder = new Request.Builder()
                    .url(SUPABASE_URL + "/rest/v1/products?select=*")
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Content-Type", "application/json")
                    .get();
                
                if (!accessToken.isEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
                }
                
                Request request = requestBuilder.build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        Log.d(TAG, "Supabase response received, length: " + responseBody.length());
                        
                        JSONArray productsArray = new JSONArray(responseBody);
                        Log.d(TAG, "Found " + productsArray.length() + " products in database");
                        
                        List<Product> products = new ArrayList<>();
                        for (int i = 0; i < productsArray.length(); i++) {
                            try {
                                JSONObject productJson = productsArray.getJSONObject(i);
                                Product product = gson.fromJson(productJson.toString(), Product.class);
                                products.add(product);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing product from database at index " + i + ": " + e.getMessage());
                                // Continue with next product
                            }
                        }
                        
                        Log.d(TAG, "Successfully parsed " + products.size() + " products from database");
                        callback.onSuccess(products);
                    } else {
                        String errorBody = response.body() != null ? response.body().string() : "";
                        Log.e(TAG, "Supabase request failed: " + response.code() + " - " + errorBody);
                        
                        if (response.code() == 404) {
                            callback.onError("Products table not found. Please run the SQL script in your Supabase dashboard first.");
                        } else {
                            callback.onError("Failed to fetch products from database: " + response.code() + " - " + errorBody);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching products from Supabase: " + e.getMessage());
                callback.onError("Error fetching products: " + e.getMessage());
            }
        }).start();
    }
    
    // Search products in Supabase database
    public static void searchProductsInSupabase(String query, ProductCallback callback) {
        new Thread(() -> {
            try {
                // Check if initialized
                if (httpClient == null) {
                    callback.onError("ProductDatabaseManager not initialized. Call initialize() first.");
                    return;
                }
                
                String accessToken = SupabaseManager.getStoredAccessToken();
                if (accessToken == null || accessToken.isEmpty()) {
                    accessToken = "";
                }
                
                String url = SUPABASE_URL + "/rest/v1/products?select=*&or=(title.ilike.*" + query + "*,description.ilike.*" + query + "*,brand.ilike.*" + query + "*,category.ilike.*" + query + "*)";
                
                Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Content-Type", "application/json")
                    .get();
                
                if (!accessToken.isEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
                }
                
                Request request = requestBuilder.build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        JSONArray productsArray = new JSONArray(responseBody);
                        
                        List<Product> products = new ArrayList<>();
                        for (int i = 0; i < productsArray.length(); i++) {
                            JSONObject productJson = productsArray.getJSONObject(i);
                            Product product = gson.fromJson(productJson.toString(), Product.class);
                            products.add(product);
                        }
                        
                        callback.onSuccess(products);
                    } else {
                        callback.onError("Failed to search products: " + response.code());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error searching products: " + e.getMessage());
                callback.onError("Error searching products: " + e.getMessage());
            }
        }).start();
    }
    
    // Get product by ID from Supabase
    public static void getProductById(int productId, ProductCallback callback) {
        new Thread(() -> {
            try {
                // Check if initialized
                if (httpClient == null) {
                    callback.onError("ProductDatabaseManager not initialized. Call initialize() first.");
                    return;
                }
                
                String accessToken = SupabaseManager.getStoredAccessToken();
                if (accessToken == null || accessToken.isEmpty()) {
                    accessToken = "";
                }
                
                String url = SUPABASE_URL + "/rest/v1/products?select=*&id=eq." + productId;
                
                Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Content-Type", "application/json")
                    .get();
                
                if (!accessToken.isEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
                }
                
                Request request = requestBuilder.build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        JSONArray productsArray = new JSONArray(responseBody);
                        
                        if (productsArray.length() > 0) {
                            JSONObject productJson = productsArray.getJSONObject(0);
                            Product product = gson.fromJson(productJson.toString(), Product.class);
                            List<Product> products = new ArrayList<>();
                            products.add(product);
                            callback.onSuccess(products);
                        } else {
                            callback.onError("Product not found");
                        }
                    } else {
                        callback.onError("Failed to get product: " + response.code());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting product by ID: " + e.getMessage());
                callback.onError("Error getting product: " + e.getMessage());
            }
        }).start();
    }
    
    // Update product in Supabase
    public static void updateProduct(Product product, DatabaseCallback callback) {
        new Thread(() -> {
            try {
                // Check if initialized
                if (httpClient == null) {
                    callback.onError("ProductDatabaseManager not initialized. Call initialize() first.");
                    return;
                }
                
                String accessToken = SupabaseManager.getStoredAccessToken();
                if (accessToken == null || accessToken.isEmpty()) {
                    accessToken = "";
                }
                
                JSONObject productJson = new JSONObject();
                productJson.put("title", product.getTitle());
                productJson.put("description", product.getDescription());
                productJson.put("price", product.getPrice());
                productJson.put("discount_percentage", product.getDiscountPercentage());
                productJson.put("rating", product.getRating());
                productJson.put("stock", product.getStock());
                productJson.put("brand", product.getBrand());
                productJson.put("category", product.getCategory());
                productJson.put("thumbnail", product.getThumbnail());
                productJson.put("images", new JSONArray(product.getImages()));
                
                RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"), 
                    productJson.toString()
                );
                
                String url = SUPABASE_URL + "/rest/v1/products?id=eq." + product.getId();
                
                Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=minimal")
                    .patch(body);
                
                if (!accessToken.isEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
                }
                
                Request request = requestBuilder.build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        callback.onSuccess("Product updated successfully");
                    } else {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        callback.onError("Failed to update product: " + response.code() + " - " + responseBody);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating product: " + e.getMessage());
                callback.onError("Error updating product: " + e.getMessage());
            }
        }).start();
    }
    
    // Delete product in Supabase
    public static void deleteProduct(int productId, DatabaseCallback callback) {
        new Thread(() -> {
            try {
                // Check if initialized
                if (httpClient == null) {
                    callback.onError("ProductDatabaseManager not initialized. Call initialize() first.");
                    return;
                }
                
                String accessToken = SupabaseManager.getStoredAccessToken();
                if (accessToken == null || accessToken.isEmpty()) {
                    accessToken = "";
                }
                
                String url = SUPABASE_URL + "/rest/v1/products?id=eq." + productId;
                
                Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Content-Type", "application/json")
                    .delete();
                
                if (!accessToken.isEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
                }
                
                Request request = requestBuilder.build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        callback.onSuccess("Product deleted successfully");
                    } else {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        callback.onError("Failed to delete product: " + response.code() + " - " + responseBody);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting product: " + e.getMessage());
                callback.onError("Error deleting product: " + e.getMessage());
            }
        }).start();
    }
    

    
    // Callback interfaces
    public interface ProductCallback {
        void onSuccess(List<Product> products);
        void onError(String error);
    }
    
    public interface DatabaseCallback {
        void onSuccess(String message);
        void onError(String error);
    }
} 