package com.praneet.neo;

import android.content.Context;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.IOException;
import okhttp3.*;
import java.util.concurrent.TimeUnit;
import android.content.SharedPreferences;
import okhttp3.HttpUrl;
import java.util.ArrayList;
import java.util.List;
import com.praneet.neo.model.CartItem;

public class SupabaseManager {
    private static final String TAG = "SupabaseManager";
    private static final String SUPABASE_URL = "https://xqofxqnwohkpbdtpotya.supabase.co";
    public static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inhxb2Z4cW53b2hrcGJkdHBvdHlhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTI5NDU0OTEsImV4cCI6MjA2ODUyMTQ5MX0.KZIo2Zuq3cxBZOliRrerPCQP7OeoRrehbXMxR4o6gIk";
    
    private static OkHttpClient httpClient;
    private static Context context;
    
    public static void initialize(Context appContext) {
        context = appContext.getApplicationContext();
        if (httpClient == null) {
            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();
        }
    }
    
    // Sign up user
    public static void signUp(String email, String password, String name, String phone, String address, AuthCallback callback) {
        new Thread(() -> {
            try {
                // Create JSON for signup
                JSONObject signupData = new JSONObject();
                signupData.put("email", email);
                signupData.put("password", password);
                
                RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"), 
                    signupData.toString()
                );
                
                Request request = new Request.Builder()
                    .url(SUPABASE_URL + "/auth/v1/signup")
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        // After successful signup, automatically sign in the user
                        signInAfterSignup(email, password, name, phone, address, callback);
                    } else {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        
                        // Extract user-friendly error message
                        String errorMessage = "Sign up failed";
                        try {
                            JSONObject errorJson = new JSONObject(responseBody);
                            String msg = errorJson.optString("msg", "");
                            if (!msg.isEmpty()) {
                                errorMessage = msg;
                            } else {
                                errorMessage = "Sign up failed: " + response.code();
                            }
                        } catch (Exception e) {
                            errorMessage = "Sign up failed: " + response.code();
                        }
                        
                        callback.onError(errorMessage);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Sign up error: " + e.getMessage());
                callback.onError("Sign up failed: " + e.getMessage());
            }
        }).start();
    }
    
    // Sign in user after signup
    private static void signInAfterSignup(String email, String password, String name, String phone, String address, AuthCallback callback) {
        try {
            JSONObject signinData = new JSONObject();
            signinData.put("email", email);
            signinData.put("password", password);
            
            RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), 
                signinData.toString()
            );
            
            Request request = new Request.Builder()
                .url(SUPABASE_URL + "/auth/v1/token?grant_type=password")
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    
                    // Extract access token
                    String accessToken = jsonResponse.optString("access_token", "");
                    
                    if (!accessToken.isEmpty()) {
                        // Store access token and user email
                        SharedPreferences prefs = context.getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
                        prefs.edit()
                            .putString("access_token", accessToken)
                            .putString("user_email", email)
                            .apply();
                        
                        callback.onSuccess("Account created successfully! You can now sign in.");
                    } else {
                        callback.onSuccess("Account created successfully! You can now sign in.");
                    }
                } else {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    callback.onSuccess("Account created successfully! You can now sign in.");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Sign in after signup error: " + e.getMessage());
            callback.onSuccess("Account created successfully! You can now sign in.");
        }
    }
    
    // Sign in user
    public static void signIn(String email, String password, String name, String phone, String address, AuthCallback callback) {
        new Thread(() -> {
            try {
                JSONObject signinData = new JSONObject();
                signinData.put("email", email);
                signinData.put("password", password);
                
                RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"), 
                    signinData.toString()
                );
                
                Request request = new Request.Builder()
                    .url(SUPABASE_URL + "/auth/v1/token?grant_type=password")
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        
                        // Extract access token
                        String accessToken = jsonResponse.optString("access_token", "");
                        
                        if (!accessToken.isEmpty()) {
                            // Store access token and user email
                            SharedPreferences prefs = context.getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
                            prefs.edit()
                                .putString("access_token", accessToken)
                                .putString("user_email", email)
                                .apply();
                            
                            callback.onSuccess("Signed in successfully!");
                        } else {
                            callback.onSuccess("Signed in successfully!");
                        }
                    } else {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        
                        // Extract user-friendly error message
                        String errorMessage = "Sign in failed";
                        try {
                            JSONObject errorJson = new JSONObject(responseBody);
                            String msg = errorJson.optString("msg", "");
                            if (!msg.isEmpty()) {
                                errorMessage = msg;
                            } else {
                                errorMessage = "Sign in failed: " + response.code();
                            }
                        } catch (Exception e) {
                            errorMessage = "Sign in failed: " + response.code();
                        }
                        
                        callback.onError(errorMessage);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Sign in error: " + e.getMessage());
                callback.onError("Sign in failed: " + e.getMessage());
            }
        }).start();
    }
    
    // Sign in user (overloaded for existing users)
    public static void signIn(String email, String password, AuthCallback callback) {
        signIn(email, password, "", "", "", callback);
    }
    
    // Sign out user
    public static void signOut(AuthCallback callback) {
        new Thread(() -> {
            try {
                String accessToken = getStoredAccessToken();
                
                if (accessToken != null && !accessToken.isEmpty()) {
                    Request request = new Request.Builder()
                        .url(SUPABASE_URL + "/auth/v1/logout")
                        .addHeader("apikey", SUPABASE_ANON_KEY)
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .addHeader("Content-Type", "application/json")
                        .post(RequestBody.create(MediaType.parse("application/json"), "{}"))
                        .build();
                    
                    try (Response response = httpClient.newCall(request).execute()) {
                        // Clear local data regardless of server response
                        clearStoredData();
                        
                        if (response.isSuccessful()) {
                            callback.onSuccess("Signed out successfully!");
                        } else {
                            // Even if server logout fails, we've cleared local data
                            callback.onSuccess("Signed out successfully!");
                        }
                    }
                } else {
                    // No token stored, just clear any remaining data
                    clearStoredData();
                    callback.onSuccess("Signed out successfully!");
                }
            } catch (Exception e) {
                Log.e(TAG, "Sign out error: " + e.getMessage());
                // Clear local data even if there's an error
                clearStoredData();
                callback.onSuccess("Signed out successfully!");
            }
        }).start();
    }
    
    // Clear stored user data
    private static void clearStoredData() {
        SharedPreferences prefs = context.getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
    
    // Extract user ID from JWT token
    private static String extractUserIdFromToken(String token) {
        try {
            // JWT tokens have 3 parts separated by dots
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                // Decode the payload (second part)
                String payload = parts[1];
                
                // Replace URL-safe characters back to standard base64
                payload = payload.replace("-", "+").replace("_", "/");
                
                // Add padding if needed
                while (payload.length() % 4 != 0) {
                    payload += "=";
                }
                
                // Decode base64
                byte[] decodedBytes = android.util.Base64.decode(payload, android.util.Base64.DEFAULT);
                String decodedPayload = new String(decodedBytes, "UTF-8");
                
                Log.d(TAG, "Decoded JWT payload: " + decodedPayload);
                
                // Parse JSON to get user ID
                JSONObject jsonPayload = new JSONObject(decodedPayload);
                String userId = jsonPayload.optString("sub", "");
                
                Log.d(TAG, "Extracted user ID: " + userId);
                return userId;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting user ID from token: " + e.getMessage());
        }
        return "";
    }
    
    // Save user profile with access token
    private static void saveUserProfileWithToken(String accessToken, String name, String phone, String address, AuthCallback callback) {
        try {
            // Extract user ID from token
            String userId = extractUserIdFromToken(accessToken);
            Log.d(TAG, "Extracted user ID from token: " + userId);
            
            if (userId.isEmpty()) {
                callback.onError("Could not extract user ID from token");
                return;
            }
            
            // Store user ID for future use
            SharedPreferences prefs = context.getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
            prefs.edit().putString("user_id", userId).apply();
            
            JSONObject profileData = new JSONObject();
            profileData.put("id", userId);
            profileData.put("name", name);
            profileData.put("phone", phone);
            profileData.put("address", address);
            Log.d("ProfileCreate", "POST body: " + profileData.toString());
            RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), 
                profileData.toString()
            );
            
            Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/profiles")
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .post(body)
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    Log.d("ProfileCreate", "Profile created successfully! Name: " + name);
                    callback.onSuccess("Account created and profile saved successfully!");
                } else {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    Log.e("ProfileCreate", "Profile create failed: " + response.code() + " - " + responseBody);
                    callback.onSuccess("Account created successfully! Profile will be saved when you sign in.");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Profile save error: " + e.getMessage());
            callback.onSuccess("Account created successfully! Profile will be saved when you sign in.");
        }
    }
    
    // Update user profile with registration data
    public static void updateUserProfile(String name, String phone, String address, AuthCallback callback) {
        new Thread(() -> {
            try {
                String accessToken = getStoredAccessToken();
                if (accessToken == null || accessToken.isEmpty()) {
                    callback.onError("Not signed in");
                    return;
                }
                
                String userId = getCurrentUserId();
                if (userId.isEmpty()) {
                    userId = extractUserIdFromToken(accessToken);
                    if (userId.isEmpty()) {
                        callback.onError("Could not determine user ID");
                        return;
                    }
                }
                
                JSONObject profileData = new JSONObject();
                profileData.put("name", name);
                profileData.put("phone", phone);
                profileData.put("address", address);
                Log.d("ProfileUpdate", "PATCH body: " + profileData.toString());
                if (name.isEmpty() && phone.isEmpty() && address.isEmpty()) {
                    Log.e("ProfileUpdate", "Attempted to PATCH with all empty fields. Aborting.");
                    callback.onError("Cannot update profile with all empty fields.");
                    return;
                }
                
                RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"), 
                    profileData.toString()
                );
                
                Request request = new Request.Builder()
                    .url(SUPABASE_URL + "/rest/v1/profiles?id=eq." + userId)
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=minimal")
                    .patch(body)
                    .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        Log.d("ProfileUpdate", "Profile updated successfully! Name: " + name);
                        callback.onSuccess("Profile updated successfully!");
                    } else {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        Log.e("ProfileUpdate", "Update failed: " + response.code() + " - " + responseBody);
                        callback.onError("Failed to update profile: " + response.code());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Profile update error: " + e.getMessage());
                callback.onError("Failed to update profile: " + e.getMessage());
            }
        }).start();
    }
    
    // Get stored access token
    public static String getStoredAccessToken() {
        SharedPreferences prefs = context.getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
        return prefs.getString("access_token", "");
    }
    
    // Check if user is signed in
    public static boolean isSignedIn() {
        String token = getStoredAccessToken();
        return token != null && !token.isEmpty();
    }
    
    // Get user ID from Supabase user endpoint
    private static String getUserInfoFromSupabase(String accessToken, ProfileCallback callback) {
        try {
            Request request = new Request.Builder()
                .url(SUPABASE_URL + "/auth/v1/user")
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .get()
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    JSONObject userInfo = new JSONObject(responseBody);
                    String userId = userInfo.optString("id", "");
                    
                    Log.d(TAG, "Got user ID from Supabase: " + userId);
                    return userId;
                } else {
                    Log.e(TAG, "Failed to get user info: " + response.code());
                    return "";
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user info: " + e.getMessage());
            return "";
        }
    }
    
    // Fetch user profile data
    public static void fetchUserProfile(ProfileCallback callback) {
        new Thread(() -> {
            try {
                String accessToken = getStoredAccessToken();
                if (accessToken == null || accessToken.isEmpty()) {
                    callback.onError("Not signed in");
                    return;
                }
                
                // Get current user ID from stored data or extract from token
                String userId = getCurrentUserId();
                if (userId.isEmpty()) {
                    // Try to extract from current token
                    userId = extractUserIdFromToken(accessToken);
                    if (userId.isEmpty()) {
                        // Try to get from Supabase user endpoint
                        userId = getUserInfoFromSupabase(accessToken, callback);
                        if (userId.isEmpty()) {
                            callback.onError("Could not determine user ID");
                            return;
                        }
                    }
                    // Store the extracted user ID
                    SharedPreferences prefs = context.getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
                    prefs.edit().putString("user_id", userId).apply();
                }
                
                Log.d("ProfileFetch", "Fetching profile for user ID: " + userId);
                
                Request request = new Request.Builder()
                    .url(SUPABASE_URL + "/rest/v1/profiles?select=*&id=eq." + userId)
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        Log.d("ProfileFetch", "Profile response: " + responseBody);
                        
                        JSONArray profiles = new JSONArray(responseBody);
                        
                        if (profiles.length() > 0) {
                            JSONObject profile = profiles.getJSONObject(0);
                            String name = profile.optString("name", "");
                            String phone = profile.optString("phone", "");
                            String address = profile.optString("address", "");
                            
                            Log.d(TAG, "Found profile: " + name + ", " + phone + ", " + address);
                            callback.onSuccess(name, phone, address);
                        } else {
                            Log.d(TAG, "No profile found for user ID: " + userId + ". Creating new profile...");
                            // Try to create a profile with basic info
                            createBasicProfile(accessToken, userId, callback);
                        }
                    } else {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        Log.e(TAG, "Profile fetch failed: " + response.code() + " - " + responseBody);
                        callback.onError("Failed to fetch profile: " + response.code() + " - " + responseBody);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Fetch profile error: " + e.getMessage());
                callback.onError("Failed to fetch profile: " + e.getMessage());
            }
        }).start();
    }
    
    // Create a basic profile for the user
    private static void createBasicProfile(String accessToken, String userId, ProfileCallback callback) {
        try {
            JSONObject profileData = new JSONObject();
            profileData.put("id", userId);
            profileData.put("name", "User");
            profileData.put("phone", "");
            profileData.put("address", "");
            Log.d("ProfileCreate", "POST body (basic): " + profileData.toString());
            RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), 
                profileData.toString()
            );
            
            Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/profiles")
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Created basic profile for user: " + userId);
                    callback.onSuccess("User", "", "");
                } else {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    Log.e(TAG, "Failed to create profile: " + response.code() + " - " + responseBody);
                    callback.onError("Profile not found and could not create new one");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating profile: " + e.getMessage());
            callback.onError("Profile not found and could not create new one");
        }
    }
    
    // Profile callback interface
    public interface ProfileCallback {
        void onSuccess(String name, String phone, String address);
        void onError(String error);
    }
    
    // Callback interface
    public interface AuthCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    // Get current user email
    public static String getCurrentUserEmail() {
        SharedPreferences prefs = context.getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
        return prefs.getString("user_email", "");
    }
    
    // Get current user ID
    public static String getCurrentUserId() {
        SharedPreferences prefs = context.getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
        return prefs.getString("user_id", "");
    }

    // Store user registration data
    public static void storeUserData(String name, String phone, String address) {
        SharedPreferences prefs = context.getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
        prefs.edit()
            .putString("user_name", name)
            .putString("user_phone", phone)
            .putString("user_address", address)
            .apply();
    }
    
    // Get stored user name
    public static String getStoredUserName() {
        SharedPreferences prefs = context.getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
        return prefs.getString("user_name", "");
    }
    
    // Get stored user phone
    public static String getStoredUserPhone() {
        SharedPreferences prefs = context.getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
        return prefs.getString("user_phone", "");
    }
    
    // Get stored user address
    public static String getStoredUserAddress() {
        SharedPreferences prefs = context.getSharedPreferences("SupabasePrefs", Context.MODE_PRIVATE);
        return prefs.getString("user_address", "");
    }

    // Add a favorite
    public static void addFavorite(long productId, AuthCallback callback) {
        new Thread(() -> {
            try {
                String userId = getCurrentUserId();
                if (userId == null || userId.isEmpty()) {
                    callback.onError("User not logged in");
                    return;
                }
                JSONObject bodyJson = new JSONObject();
                bodyJson.put("user_id", userId);
                bodyJson.put("product_id", productId);

                RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"), bodyJson.toString());

                Request request = new Request.Builder()
                    .url(SUPABASE_URL + "/rest/v1/favorites")
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer " + getStoredAccessToken())
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        callback.onSuccess("Added to favorites");
                    } else {
                        callback.onError("Failed to add favorite: " + response.code());
                    }
                }
            } catch (Exception e) {
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }

    // Remove a favorite
    public static void removeFavorite(long productId, AuthCallback callback) {
        new Thread(() -> {
            try {
                String userId = getCurrentUserId();
                if (userId == null || userId.isEmpty()) {
                    callback.onError("User not logged in");
                    return;
                }
                HttpUrl url = HttpUrl.parse(SUPABASE_URL + "/rest/v1/favorites")
                    .newBuilder()
                    .addQueryParameter("user_id", "eq." + userId)
                    .addQueryParameter("product_id", "eq." + productId)
                    .build();

                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer " + getStoredAccessToken())
                    .delete()
                    .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        callback.onSuccess("Removed from favorites");
                    } else {
                        callback.onError("Failed to remove favorite: " + response.code());
                    }
                }
            } catch (Exception e) {
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }

    // Get all favorite product IDs for the current user
    public static void getFavorites(FavoritesCallback callback) {
        new Thread(() -> {
            try {
                String userId = getCurrentUserId();
                if (userId == null || userId.isEmpty()) {
                    callback.onError("User not logged in");
                    return;
                }
                HttpUrl url = HttpUrl.parse(SUPABASE_URL + "/rest/v1/favorites")
                    .newBuilder()
                    .addQueryParameter("user_id", "eq." + userId)
                    .build();

                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer " + getStoredAccessToken())
                    .get()
                    .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        JSONArray arr = new JSONArray(responseBody);
                        List<Long> productIds = new ArrayList<>();
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            productIds.add(obj.getLong("product_id"));
                        }
                        callback.onSuccess(productIds);
                    } else {
                        callback.onError("Failed to fetch favorites: " + response.code());
                    }
                }
            } catch (Exception e) {
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }

    // Callback for getting favorites
    public interface FavoritesCallback {
        void onSuccess(List<Long> productIds);
        void onError(String error);
    }

    // Place an order for the current user
    public static void placeOrder(List<CartItem> cartItems, double totalPrice, AuthCallback callback) {
        new Thread(() -> {
            try {
                String userId = getCurrentUserId();
                if (userId == null || userId.isEmpty()) {
                    callback.onError("User not logged in");
                    return;
                }
                // 1. Create order
                JSONObject orderJson = new JSONObject();
                orderJson.put("user_id", userId);
                orderJson.put("total_price", totalPrice);
                // status and created_at are default

                RequestBody orderBody = RequestBody.create(
                    MediaType.parse("application/json"), orderJson.toString());

                Request orderRequest = new Request.Builder()
                    .url(SUPABASE_URL + "/rest/v1/orders?select=id")
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer " + getStoredAccessToken())
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .post(orderBody)
                    .build();

                try (Response orderResponse = httpClient.newCall(orderRequest).execute()) {
                    if (!orderResponse.isSuccessful()) {
                        callback.onError("Failed to create order: " + orderResponse.code());
                        return;
                    }
                    String orderRespBody = orderResponse.body() != null ? orderResponse.body().string() : "";
                    JSONArray arr = new JSONArray(orderRespBody);
                    if (arr.length() == 0) {
                        callback.onError("Order creation failed: no ID returned");
                        return;
                    }
                    long orderId = arr.getJSONObject(0).getLong("id");

                    // 2. Add order_items
                    JSONArray itemsArray = new JSONArray();
                    for (CartItem item : cartItems) {
                        JSONObject itemJson = new JSONObject();
                        itemJson.put("order_id", orderId);
                        itemJson.put("product_id", item.getProduct().getId());
                        itemJson.put("quantity", item.getQuantity());
                        itemJson.put("price_at_purchase", item.getProduct().getPrice());
                        itemsArray.put(itemJson);
                    }
                    RequestBody itemsBody = RequestBody.create(
                        MediaType.parse("application/json"), itemsArray.toString());
                    Request itemsRequest = new Request.Builder()
                        .url(SUPABASE_URL + "/rest/v1/order_items")
                        .addHeader("apikey", SUPABASE_ANON_KEY)
                        .addHeader("Authorization", "Bearer " + getStoredAccessToken())
                        .addHeader("Content-Type", "application/json")
                        .post(itemsBody)
                        .build();
                    try (Response itemsResponse = httpClient.newCall(itemsRequest).execute()) {
                        if (!itemsResponse.isSuccessful()) {
                            callback.onError("Failed to add order items: " + itemsResponse.code());
                            return;
                        }
                        callback.onSuccess("Order placed successfully");
                    }
                }
            } catch (Exception e) {
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }

    // Get all orders for the current user
    public static void getOrdersForCurrentUser(OrdersCallback callback) {
        new Thread(() -> {
            try {
                String userId = getCurrentUserId();
                if (userId == null || userId.isEmpty()) {
                    callback.onError("User not logged in");
                    return;
                }
                HttpUrl url = HttpUrl.parse(SUPABASE_URL + "/rest/v1/orders")
                    .newBuilder()
                    .addQueryParameter("user_id", "eq." + userId)
                    .addQueryParameter("order", "created_at.desc")
                    .build();
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer " + getStoredAccessToken())
                    .get()
                    .build();
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        callback.onSuccess(new JSONArray(responseBody));
                    } else {
                        callback.onError("Failed to fetch orders: " + response.code());
                    }
                }
            } catch (Exception e) {
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }

    // Get all items for a given order
    public static void getOrderItems(long orderId, OrderItemsCallback callback) {
        new Thread(() -> {
            try {
                HttpUrl url = HttpUrl.parse(SUPABASE_URL + "/rest/v1/order_items")
                    .newBuilder()
                    .addQueryParameter("order_id", "eq." + orderId)
                    .build();
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer " + getStoredAccessToken())
                    .get()
                    .build();
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        callback.onSuccess(new JSONArray(responseBody));
                    } else {
                        callback.onError("Failed to fetch order items: " + response.code());
                    }
                }
            } catch (Exception e) {
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }

    // Callback for getting orders
    public interface OrdersCallback {
        void onSuccess(JSONArray orders);
        void onError(String error);
    }
    // Callback for getting order items
    public interface OrderItemsCallback {
        void onSuccess(JSONArray orderItems);
        void onError(String error);
    }

    // Cart callbacks for backend-only cart operations
    public interface CartCallback {
        void onSuccess();
        void onError(String error);
    }
    public interface CartItemsCallback {
        void onSuccess(List<com.praneet.neo.model.CartItem> cartItems);
        void onError(String error);
    }

    // --- CART BACKEND METHODS (REAL IMPLEMENTATION) ---
    public static void addToCart(com.praneet.neo.model.Product product, int quantity, CartCallback callback) {
        new Thread(() -> {
            try {
                String userId = getCurrentUserId();
                if (userId == null || userId.isEmpty()) {
                    callback.onError("User not logged in");
                    return;
                }
                // Upsert: if item exists, update quantity; else insert
                // Try to fetch existing cart item
                okhttp3.HttpUrl url = okhttp3.HttpUrl.parse(SUPABASE_URL + "/rest/v1/cart")
                    .newBuilder()
                    .addQueryParameter("user_id", "eq." + userId)
                    .addQueryParameter("product_id", "eq." + product.getId())
                    .build();
                Request getReq = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer " + getStoredAccessToken())
                    .get()
                    .build();
                try (Response getResp = httpClient.newCall(getReq).execute()) {
                    if (getResp.isSuccessful()) {
                        String respBody = getResp.body() != null ? getResp.body().string() : "";
                        org.json.JSONArray arr = new org.json.JSONArray(respBody);
                        if (arr.length() > 0) {
                            // Exists: update quantity
                            org.json.JSONObject obj = arr.getJSONObject(0);
                            int existingQty = obj.getInt("quantity");
                            int newQty = existingQty + quantity;
                            int cartId = obj.getInt("id");
                            org.json.JSONObject patch = new org.json.JSONObject();
                            patch.put("quantity", newQty);
                            RequestBody patchBody = RequestBody.create(
                                MediaType.parse("application/json"), patch.toString());
                            Request patchReq = new Request.Builder()
                                .url(SUPABASE_URL + "/rest/v1/cart?id=eq." + cartId)
                                .addHeader("apikey", SUPABASE_ANON_KEY)
                                .addHeader("Authorization", "Bearer " + getStoredAccessToken())
                                .addHeader("Content-Type", "application/json")
                                .patch(patchBody)
                                .build();
                            try (Response patchResp = httpClient.newCall(patchReq).execute()) {
                                if (patchResp.isSuccessful()) {
                                    callback.onSuccess();
                                } else {
                                    callback.onError("Failed to update cart: " + patchResp.code());
                                }
                            }
                        } else {
                            // Not exists: insert new
                            org.json.JSONObject cartItem = new org.json.JSONObject();
                            cartItem.put("user_id", userId);
                            cartItem.put("product_id", product.getId());
                            cartItem.put("quantity", quantity);
                            RequestBody body = RequestBody.create(
                                MediaType.parse("application/json"), cartItem.toString());
                            Request postReq = new Request.Builder()
                                .url(SUPABASE_URL + "/rest/v1/cart")
                                .addHeader("apikey", SUPABASE_ANON_KEY)
                                .addHeader("Authorization", "Bearer " + getStoredAccessToken())
                                .addHeader("Content-Type", "application/json")
                                .addHeader("Prefer", "return=representation")
                                .post(body)
                                .build();
                            try (Response postResp = httpClient.newCall(postReq).execute()) {
                                if (postResp.isSuccessful()) {
                                    callback.onSuccess();
                                } else {
                                    callback.onError("Failed to add to cart: " + postResp.code());
                                }
                            }
                        }
                    } else {
                        callback.onError("Failed to check cart: " + getResp.code());
                    }
                }
            } catch (Exception e) {
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }

    public static void getCartItems(CartItemsCallback callback) {
        new Thread(() -> {
            try {
                String userId = getCurrentUserId();
                if (userId == null || userId.isEmpty()) {
                    callback.onError("User not logged in");
                    return;
                }
                okhttp3.HttpUrl url = okhttp3.HttpUrl.parse(SUPABASE_URL + "/rest/v1/cart")
                    .newBuilder()
                    .addQueryParameter("user_id", "eq." + userId)
                    .build();
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer " + getStoredAccessToken())
                    .get()
                    .build();
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        org.json.JSONArray arr = new org.json.JSONArray(responseBody);
                        List<Integer> productIds = new java.util.ArrayList<>();
                        List<Integer> quantities = new java.util.ArrayList<>();
                        for (int i = 0; i < arr.length(); i++) {
                            org.json.JSONObject obj = arr.getJSONObject(i);
                            int productId = obj.getInt("product_id");
                            int quantity = obj.getInt("quantity");
                            productIds.add(productId);
                            quantities.add(quantity);
                        }
                        if (productIds.isEmpty()) {
                            callback.onSuccess(new java.util.ArrayList<>());
                            return;
                        }
                        fetchProductsForCart(productIds, quantities, new java.util.ArrayList<>(), 0, callback);
                    } else {
                        callback.onError("Failed to fetch cart: " + response.code());
                    }
                }
            } catch (Exception e) {
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }

    // Helper method to fetch products for cart items recursively
    private static void fetchProductsForCart(List<Integer> productIds, List<Integer> quantities, List<com.praneet.neo.model.CartItem> cartItems, int index, CartItemsCallback callback) {
        if (index >= productIds.size()) {
            callback.onSuccess(cartItems);
            return;
        }
        int productId = productIds.get(index);
        int quantity = quantities.get(index);
        ProductDatabaseManager.getProductById(productId, new ProductDatabaseManager.ProductCallback() {
            @Override
            public void onSuccess(List<com.praneet.neo.model.Product> products) {
                if (!products.isEmpty()) {
                    cartItems.add(new com.praneet.neo.model.CartItem(products.get(0), quantity));
                }
                fetchProductsForCart(productIds, quantities, cartItems, index + 1, callback);
            }
            @Override
            public void onError(String error) {
                // Skip this product and continue
                fetchProductsForCart(productIds, quantities, cartItems, index + 1, callback);
            }
        });
    }

    public static void updateCartQuantity(int productId, int quantity, CartCallback callback) {
        new Thread(() -> {
            try {
                String userId = getCurrentUserId();
                if (userId == null || userId.isEmpty()) {
                    callback.onError("User not logged in");
                    return;
                }
                // Find cart item by user_id and product_id
                okhttp3.HttpUrl url = okhttp3.HttpUrl.parse(SUPABASE_URL + "/rest/v1/cart")
                    .newBuilder()
                    .addQueryParameter("user_id", "eq." + userId)
                    .addQueryParameter("product_id", "eq." + productId)
                    .build();
                Request getReq = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer " + getStoredAccessToken())
                    .get()
                    .build();
                try (Response getResp = httpClient.newCall(getReq).execute()) {
                    if (getResp.isSuccessful()) {
                        String respBody = getResp.body() != null ? getResp.body().string() : "";
                        org.json.JSONArray arr = new org.json.JSONArray(respBody);
                        if (arr.length() > 0) {
                            org.json.JSONObject obj = arr.getJSONObject(0);
                            int cartId = obj.getInt("id");
                            if (quantity <= 0) {
                                // Remove item
                                removeFromCart(productId, callback);
                                return;
                            }
                            org.json.JSONObject patch = new org.json.JSONObject();
                            patch.put("quantity", quantity);
                            RequestBody patchBody = RequestBody.create(
                                MediaType.parse("application/json"), patch.toString());
                            Request patchReq = new Request.Builder()
                                .url(SUPABASE_URL + "/rest/v1/cart?id=eq." + cartId)
                                .addHeader("apikey", SUPABASE_ANON_KEY)
                                .addHeader("Authorization", "Bearer " + getStoredAccessToken())
                                .addHeader("Content-Type", "application/json")
                                .patch(patchBody)
                                .build();
                            try (Response patchResp = httpClient.newCall(patchReq).execute()) {
                                if (patchResp.isSuccessful()) {
                                    callback.onSuccess();
                                } else {
                                    callback.onError("Failed to update cart: " + patchResp.code());
                                }
                            }
                        } else {
                            callback.onError("Cart item not found");
                        }
                    } else {
                        callback.onError("Failed to check cart: " + getResp.code());
                    }
                }
            } catch (Exception e) {
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }

    public static void removeFromCart(int productId, CartCallback callback) {
        new Thread(() -> {
            try {
                String userId = getCurrentUserId();
                if (userId == null || userId.isEmpty()) {
                    callback.onError("User not logged in");
                    return;
                }
                // Find cart item by user_id and product_id
                okhttp3.HttpUrl url = okhttp3.HttpUrl.parse(SUPABASE_URL + "/rest/v1/cart")
                    .newBuilder()
                    .addQueryParameter("user_id", "eq." + userId)
                    .addQueryParameter("product_id", "eq." + productId)
                    .build();
                Request getReq = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer " + getStoredAccessToken())
                    .get()
                    .build();
                try (Response getResp = httpClient.newCall(getReq).execute()) {
                    if (getResp.isSuccessful()) {
                        String respBody = getResp.body() != null ? getResp.body().string() : "";
                        org.json.JSONArray arr = new org.json.JSONArray(respBody);
                        if (arr.length() > 0) {
                            org.json.JSONObject obj = arr.getJSONObject(0);
                            int cartId = obj.getInt("id");
                            Request delReq = new Request.Builder()
                                .url(SUPABASE_URL + "/rest/v1/cart?id=eq." + cartId)
                                .addHeader("apikey", SUPABASE_ANON_KEY)
                                .addHeader("Authorization", "Bearer " + getStoredAccessToken())
                                .delete()
                                .build();
                            try (Response delResp = httpClient.newCall(delReq).execute()) {
                                if (delResp.isSuccessful()) {
                                    callback.onSuccess();
                                } else {
                                    callback.onError("Failed to remove from cart: " + delResp.code());
                                }
                            }
                        } else {
                            callback.onError("Cart item not found");
                        }
                    } else {
                        callback.onError("Failed to check cart: " + getResp.code());
                    }
                }
            } catch (Exception e) {
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }

    public static void clearCart(CartCallback callback) {
        new Thread(() -> {
            try {
                String userId = getCurrentUserId();
                if (userId == null || userId.isEmpty()) {
                    callback.onError("User not logged in");
                    return;
                }
                // Get all cart items for user
                okhttp3.HttpUrl url = okhttp3.HttpUrl.parse(SUPABASE_URL + "/rest/v1/cart")
                    .newBuilder()
                    .addQueryParameter("user_id", "eq." + userId)
                    .build();
                Request getReq = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer " + getStoredAccessToken())
                    .get()
                    .build();
                try (Response getResp = httpClient.newCall(getReq).execute()) {
                    if (getResp.isSuccessful()) {
                        String respBody = getResp.body() != null ? getResp.body().string() : "";
                        org.json.JSONArray arr = new org.json.JSONArray(respBody);
                        boolean allSuccess = true;
                        for (int i = 0; i < arr.length(); i++) {
                            org.json.JSONObject obj = arr.getJSONObject(i);
                            int cartId = obj.getInt("id");
                            Request delReq = new Request.Builder()
                                .url(SUPABASE_URL + "/rest/v1/cart?id=eq." + cartId)
                                .addHeader("apikey", SUPABASE_ANON_KEY)
                                .addHeader("Authorization", "Bearer " + getStoredAccessToken())
                                .delete()
                                .build();
                            try (Response delResp = httpClient.newCall(delReq).execute()) {
                                if (!delResp.isSuccessful()) {
                                    allSuccess = false;
                                }
                            }
                        }
                        if (allSuccess) {
                            callback.onSuccess();
                        } else {
                            callback.onError("Failed to clear some cart items");
                        }
                    } else {
                        callback.onError("Failed to fetch cart: " + getResp.code());
                    }
                }
            } catch (Exception e) {
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }

    // Fetch all orders (for admin)
    public static void getAllOrders(OrdersCallback callback) {
        android.util.Log.d("AdminCheck", "Fetching all orders for admin");
        new Thread(() -> {
            try {
                okhttp3.HttpUrl url = okhttp3.HttpUrl.parse(SUPABASE_URL + "/rest/v1/orders")
                    .newBuilder()
                    .addQueryParameter("order", "created_at.desc")
                    .build();
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer " + getStoredAccessToken())
                    .get()
                    .build();
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        callback.onSuccess(new org.json.JSONArray(responseBody));
                    } else {
                        callback.onError("Failed to fetch orders: " + response.code());
                    }
                }
            } catch (Exception e) {
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }

    private static Boolean cachedIsAdmin = null;

    public interface AdminStatusCallback {
        void onResult(boolean isAdmin);
    }

    public static void fetchAndCacheAdminStatus(AdminStatusCallback callback) {
        new Thread(() -> {
            try {
                String userId = getCurrentUserId();
                android.util.Log.d("AdminCheck", "Fetching profile for userId: " + userId);
                if (userId == null || userId.isEmpty()) {
                    cachedIsAdmin = false;
                    callback.onResult(false);
                    return;
                }
                okhttp3.HttpUrl url = okhttp3.HttpUrl.parse(SUPABASE_URL + "/rest/v1/profiles")
                    .newBuilder()
                    .addQueryParameter("id", "eq." + userId)
                    .build();
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer " + getStoredAccessToken())
                    .get()
                    .build();
                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        org.json.JSONArray arr = new org.json.JSONArray(responseBody);
                        if (arr.length() > 0) {
                            org.json.JSONObject profile = arr.getJSONObject(0);
                            boolean isAdmin = profile.optBoolean("is_admin", false);
                            android.util.Log.d("AdminCheck", "Profile: " + profile.toString() + ", is_admin: " + isAdmin);
                            cachedIsAdmin = isAdmin;
                            callback.onResult(isAdmin);
                            return;
                        } else {
                            android.util.Log.d("AdminCheck", "No profile found for userId: " + userId);
                        }
                    } else {
                        android.util.Log.d("AdminCheck", "Profile fetch failed: " + response.code());
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("AdminCheck", "Exception: " + e.getMessage());
            }
            cachedIsAdmin = false;
            callback.onResult(false);
        }).start();
    }

    public static boolean isCurrentUserAdmin() {
        return cachedIsAdmin != null && cachedIsAdmin;
    }
} 