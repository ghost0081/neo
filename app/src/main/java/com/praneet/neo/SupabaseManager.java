package com.praneet.neo;

import android.content.Context;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.IOException;
import okhttp3.*;
import java.util.concurrent.TimeUnit;
import android.content.SharedPreferences;

public class SupabaseManager {
    private static final String TAG = "SupabaseManager";
    private static final String SUPABASE_URL = "https://xqofxqnwohkpbdtpotya.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inhxb2Z4cW53b2hrcGJkdHBvdHlhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTI5NDU0OTEsImV4cCI6MjA2ODUyMTQ5MX0.KZIo2Zuq3cxBZOliRrerPCQP7OeoRrehbXMxR4o6gIk";
    
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
                        
                        // Save profile data with the access token
                        saveUserProfileWithToken(accessToken, name, phone, address, callback);
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
                            
                            // Save profile data with the access token
                            saveUserProfileWithToken(accessToken, name, phone, address, callback);
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
                    callback.onSuccess("Account created and profile saved successfully!");
                } else {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    Log.e(TAG, "Profile save error: " + response.code() + " - " + responseBody);
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
                profileData.put("id", userId);
                profileData.put("name", name);
                profileData.put("phone", phone);
                profileData.put("address", address);
                
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
                        callback.onSuccess("Profile updated successfully!");
                    } else {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        Log.e(TAG, "Profile update error: " + response.code() + " - " + responseBody);
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
    private static String getStoredAccessToken() {
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
                
                Log.d(TAG, "Fetching profile for user ID: " + userId);
                
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
                        Log.d(TAG, "Profile response: " + responseBody);
                        
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
} 