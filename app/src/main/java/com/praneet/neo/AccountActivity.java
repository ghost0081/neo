package com.praneet.neo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.HttpUrl;
import org.json.JSONArray;
import org.json.JSONObject;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.view.Gravity;
import android.content.Intent;

public class AccountActivity extends AppCompatActivity {
    
    // UI Elements
    private LinearLayout loginForm, registerForm, userProfile;
    private EditText loginEmail, loginPassword;
    private EditText registerName, registerEmail, registerPhone, registerAddress, registerPassword;
    private Button loginButton, registerButton, logoutButton;
    private TextView toggleMode, loginStatus, registerStatus;
    private TextView profileName, profileEmail, profilePhone, profileAddress;
    private LinearLayout adminSection;
    
    private boolean isLoginMode = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Hide action bar if it exists
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        setContentView(R.layout.activity_account);
        
        initializeViews();
        setupClickListeners();
        checkAuthStatus();
    }
    
    private void initializeViews() {
        // Forms
        loginForm = findViewById(R.id.login_form);
        registerForm = findViewById(R.id.register_form);
        userProfile = findViewById(R.id.user_profile);
        
        // Login form elements
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        loginStatus = findViewById(R.id.login_status);
        
        // Register form elements
        registerName = findViewById(R.id.register_name);
        registerEmail = findViewById(R.id.register_email);
        registerPhone = findViewById(R.id.register_phone);
        registerAddress = findViewById(R.id.register_address);
        registerPassword = findViewById(R.id.register_password);
        registerButton = findViewById(R.id.register_button);
        registerStatus = findViewById(R.id.register_status);
        
        // Profile elements
        profileName = findViewById(R.id.profile_name);
        profileEmail = findViewById(R.id.profile_email);
        profilePhone = findViewById(R.id.profile_phone);
        profileAddress = findViewById(R.id.profile_address);
        logoutButton = findViewById(R.id.logout_button);
        
        // Toggle
        toggleMode = findViewById(R.id.toggle_mode);

        // Admin section (added programmatically)
        adminSection = new LinearLayout(this);
        adminSection.setOrientation(LinearLayout.HORIZONTAL);
        adminSection.setVisibility(View.GONE);
        adminSection.setPadding(0, 32, 0, 0);
        // Add to userProfile layout after profileAddress
        userProfile.addView(adminSection);
    }
    
    private void setupClickListeners() {
        toggleMode.setOnClickListener(v -> toggleMode());
        loginButton.setOnClickListener(v -> handleLogin());
        registerButton.setOnClickListener(v -> handleRegistration());
        logoutButton.setOnClickListener(v -> handleLogout());
        
        // Add Create New Account button functionality
        Button signupButton = findViewById(R.id.signup_button);
        if (signupButton != null) {
            Log.d("AccountActivity", "Signup button found, setting click listener");
            signupButton.setOnClickListener(v -> {
                Log.d("AccountActivity", "Create New Account button clicked!");
                showRegistrationForm();
            });
        } else {
            Log.e("AccountActivity", "Signup button not found!");
        }
        
        // Add Edit Profile button functionality with debugging
        Button editProfileButton = findViewById(R.id.edit_profile_button);
        if (editProfileButton != null) {
            Log.d("AccountActivity", "Edit Profile button found, setting click listener");
            editProfileButton.setOnClickListener(v -> {
                Log.d("AccountActivity", "Edit Profile button clicked!");
                handleEditProfile();
            });
        } else {
            Log.e("AccountActivity", "Edit Profile button not found!");
        }

        Button manageUsersButton = findViewById(R.id.button_manage_users);
        manageUsersButton.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, AdminUserActivity.class);
            startActivity(intent);
        });
    }
    
    private void toggleMode() {
        isLoginMode = !isLoginMode;
        
        if (isLoginMode) {
            loginForm.setVisibility(View.VISIBLE);
            registerForm.setVisibility(View.GONE);
            toggleMode.setText("New User? Sign Up");
        } else {
            loginForm.setVisibility(View.GONE);
            registerForm.setVisibility(View.VISIBLE);
            toggleMode.setText("Already have an account? Sign In");
        }
        
        // Clear status messages
        loginStatus.setText("");
        registerStatus.setText("");
    }
    
    private void handleLogin() {
        String email = loginEmail.getText().toString().trim();
        String password = loginPassword.getText().toString().trim();
        
        if (email.isEmpty() || password.isEmpty()) {
            loginStatus.setText("Please fill in all fields");
            loginStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            return;
        }
        
        // Email validation for login too
        if (!isValidEmail(email)) {
            loginStatus.setText("Please enter a valid email address");
            loginStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            return;
        }
        
        // Show loading
        loginButton.setEnabled(false);
        loginButton.setText("Signing In...");
        loginStatus.setText("");
        
        SupabaseManager.signIn(email, password, new SupabaseManager.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    loginStatus.setText(message);
                    loginStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    loginButton.setEnabled(true);
                    loginButton.setText("Sign In");
                    
                    // Show profile after successful login
                    showUserProfile();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    loginStatus.setText(error);
                    loginStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    loginButton.setEnabled(true);
                    loginButton.setText("Sign In");
                });
            }
        });
    }
    
    private void handleRegistration() {
        String name = registerName.getText().toString().trim();
        String email = registerEmail.getText().toString().trim();
        String phone = registerPhone.getText().toString().trim();
        String address = registerAddress.getText().toString().trim();
        String password = registerPassword.getText().toString().trim();
        
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            registerStatus.setText("Please fill in all fields");
            registerStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            return;
        }
        
        // Email validation
        if (!isValidEmail(email)) {
            registerStatus.setText("Please enter a valid email address");
            registerStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            return;
        }
        
        // Check if this is edit mode (password field is empty or button text is "Update Profile")
        boolean isEditMode = password.isEmpty() || registerButton.getText().toString().equals("Update Profile");
        
        if (isEditMode) {
            // Handle profile update
            handleProfileUpdate(name, phone, address);
        } else {
            // Handle new registration
            if (password.length() < 6) {
                registerStatus.setText("Password must be at least 6 characters");
                registerStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                return;
            }
            
            // Show loading
            registerButton.setEnabled(false);
            registerButton.setText("Creating Account...");
            registerStatus.setText("");
            
            SupabaseManager.signUp(email, password, name, phone, address, new SupabaseManager.AuthCallback() {
                @Override
                public void onSuccess(String message) {
                    runOnUiThread(() -> {
                        registerStatus.setText(message);
                        registerStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        registerButton.setEnabled(true);
                        registerButton.setText("Create Account");
                        
                        // Store user data for later use
                        SupabaseManager.storeUserData(name, phone, address);
                        
                        // Clear form
                        clearRegistrationForm();
                        
                        // Show success message
                        Toast.makeText(AccountActivity.this, message, Toast.LENGTH_LONG).show();
                        
                        // If account was created and signed in successfully, show profile
                        if (message.contains("successfully")) {
                            showUserProfile();
                        } else {
                            // Switch to login mode and pre-fill email
                            isLoginMode = true;
                            toggleMode();
                            loginEmail.setText(email);
                        }
                    });
                }
                
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        if (error.toLowerCase().contains("already registered") || error.toLowerCase().contains("user already exists") || error.toLowerCase().contains("email")) {
                            registerStatus.setText("A user with this email already exists.");
                        } else {
                            registerStatus.setText(error);
                        }
                        registerStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        registerButton.setEnabled(true);
                        registerButton.setText("Create Account");
                    });
                }
            });
        }
    }
    
    private void handleProfileUpdate(String name, String phone, String address) {
        // Show loading
        registerButton.setEnabled(false);
        registerButton.setText("Updating...");
        registerStatus.setText("");
        
        SupabaseManager.updateUserProfile(name, phone, address, new SupabaseManager.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    registerStatus.setText(message);
                    registerStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    registerButton.setEnabled(true);
                    registerButton.setText("Update Profile");
                    
                    // Store updated user data
                    SupabaseManager.storeUserData(name, phone, address);
                    
                    // Show success message
                    Toast.makeText(AccountActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    
                    // Return to profile view
                    showUserProfile();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    registerStatus.setText(error);
                    registerStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    registerButton.setEnabled(true);
                    registerButton.setText("Update Profile");
                });
            }
        });
    }
    
    private void handleLogout() {
        SupabaseManager.signOut(new SupabaseManager.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(AccountActivity.this, message, Toast.LENGTH_SHORT).show();
                    showLoginForm();
                    // Refresh cart for guest/next user
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(AccountActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void checkAuthStatus() {
        if (SupabaseManager.isSignedIn()) {
            showUserProfile();
        } else {
            showLoginForm();
        }
    }
    
    private void showLoginForm() {
        loginForm.setVisibility(View.VISIBLE);
        registerForm.setVisibility(View.GONE);
        userProfile.setVisibility(View.GONE);
        isLoginMode = true;
        toggleMode.setText("New User? Sign Up");
        toggleMode.setVisibility(View.VISIBLE); // Show toggle on login form
    }
    
    private void showUserProfile() {
        loginForm.setVisibility(View.GONE);
        registerForm.setVisibility(View.GONE);
        userProfile.setVisibility(View.VISIBLE);
        toggleMode.setVisibility(View.GONE); // Hide toggle after login
        // Refresh cart for the current user
        
        // Show loading state
        profileName.setText("Loading profile...");
        profileEmail.setText("");
        profilePhone.setText("");
        profileAddress.setText("");
        
        // Get stored user data first
        final String storedName = SupabaseManager.getStoredUserName();
        final String storedPhone = SupabaseManager.getStoredUserPhone();
        final String storedAddress = SupabaseManager.getStoredUserAddress();
        
        Log.d("AccountActivity", "Stored data - Name: " + storedName + ", Phone: " + storedPhone + ", Address: " + storedAddress);
        
        // If stored data is empty, set it manually for testing
        if (storedName.isEmpty()) {
            Log.d("AccountActivity", "No stored data found, setting test data");
            SupabaseManager.storeUserData("praneet singh", "8851271943", "hardev nagar");
        }
        
        // Fetch actual user profile from Supabase
        SupabaseManager.fetchUserProfile(new SupabaseManager.ProfileCallback() {
            @Override
            public void onSuccess(String name, String phone, String address) {
                runOnUiThread(() -> {
                    Log.d("ProfileUI", "Setting UI: name=" + name + ", phone=" + phone + ", address=" + address);
                    profileName.setText(name);
                    profileEmail.setText("Email: " + SupabaseManager.getCurrentUserEmail());
                    profilePhone.setText(phone);
                    profileAddress.setText(address);
                    Log.d("AccountActivity", "Displaying - Name: " + name + ", Phone: " + phone + ", Address: " + address);
                    // Fetch is_admin from profile
                    fetchIsAdminAndShowAdminSection();
                });
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    profileName.setText("");
                    profileEmail.setText("Email: " + SupabaseManager.getCurrentUserEmail());
                    profilePhone.setText("");
                    profileAddress.setText("");
                    Log.d("AccountActivity", "Error case - Displaying empty profile fields");
                });
            }
        });
    }
    
    private void updateProfileWithStoredData(String name, String phone, String address) {
        SupabaseManager.updateUserProfile(name, phone, address, new SupabaseManager.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(AccountActivity.this, "Profile updated: " + message, Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(AccountActivity.this, "Profile update failed: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void clearRegistrationForm() {
        registerName.setText("");
        registerEmail.setText("");
        registerPhone.setText("");
        registerAddress.setText("");
        registerPassword.setText("");
    }

    private void showRegistrationForm() {
        loginForm.setVisibility(View.GONE);
        registerForm.setVisibility(View.VISIBLE);
        userProfile.setVisibility(View.GONE);
        isLoginMode = false;
        toggleMode.setText("Already have an account? Sign In");
        toggleMode.setVisibility(View.VISIBLE); // Show toggle on registration form
        
        // Clear the form for new registration
        clearRegistrationForm();
        
        // Reset button text for new account creation
        registerButton.setText("Create Account");
        
        // Reset form title
        TextView registerTitle = findViewById(R.id.register_title);
        if (registerTitle != null) {
            registerTitle.setText("Create Account");
        }
        
        // Clear any previous status messages
        loginStatus.setText("");
        registerStatus.setText("");
        
        Log.d("AccountActivity", "Registration form displayed for new account creation");
    }

    private void handleEditProfile() {
        Log.d("AccountActivity", "handleEditProfile called");
        
        // Show registration form directly (don't use toggleMode)
        loginForm.setVisibility(View.GONE);
        registerForm.setVisibility(View.VISIBLE);
        userProfile.setVisibility(View.GONE);
        isLoginMode = false;
        toggleMode.setText("Already have an account? Sign In");
        
        // Pre-fill the form with current profile data
        String currentName = SupabaseManager.getStoredUserName();
        String currentPhone = SupabaseManager.getStoredUserPhone();
        String currentAddress = SupabaseManager.getStoredUserAddress();
        String currentEmail = SupabaseManager.getCurrentUserEmail();
        
        Log.d("AccountActivity", "Current data - Name: " + currentName + ", Phone: " + currentPhone + ", Address: " + currentAddress + ", Email: " + currentEmail);
        
        // Clear the form first
        registerName.setText("");
        registerEmail.setText("");
        registerPhone.setText("");
        registerAddress.setText("");
        registerPassword.setText("");
        
        // Then fill with current data
        registerName.setText(currentName);
        registerEmail.setText(currentEmail);
        registerPhone.setText(currentPhone);
        registerAddress.setText(currentAddress);
        
        // Change button text to indicate update mode
        registerButton.setText("Update Profile");
        
        // Update the form title
        TextView registerTitle = findViewById(R.id.register_title);
        if (registerTitle != null) {
            registerTitle.setText("Edit Profile");
            Log.d("AccountActivity", "Form title updated to 'Edit Profile'");
        } else {
            Log.e("AccountActivity", "Register title not found!");
        }
        
        // Clear any previous status messages
        registerStatus.setText("");
        
        // Show message
        Toast.makeText(this, "Edit your profile information", Toast.LENGTH_SHORT).show();
        Log.d("AccountActivity", "Edit profile form displayed");
    }

    // Email validation helper method
    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }

    private void fetchIsAdminAndShowAdminSection() {
        new Thread(() -> {
            try {
                String userId = SupabaseManager.getCurrentUserId();
                String supabaseUrl = "https://xqofxqnwohkpbdtpotya.supabase.co/rest/v1/profiles";
                HttpUrl url = HttpUrl.parse(supabaseUrl)
                    .newBuilder()
                    .addQueryParameter("id", "eq." + userId)
                    .addQueryParameter("select", "is_admin")
                    .build();
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseManager.SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer " + SupabaseManager.getStoredAccessToken())
                    .get()
                    .build();
                try (Response response = client.newCall(request).execute()) {
                    boolean isAdmin = false;
                    if (response.isSuccessful()) {
                        String body = response.body() != null ? response.body().string() : "";
                        JSONArray arr = new JSONArray(body);
                        if (arr.length() > 0) {
                            JSONObject obj = arr.getJSONObject(0);
                            isAdmin = obj.optBoolean("is_admin", false);
                        }
                    }
                    boolean finalIsAdmin = isAdmin;
                    runOnUiThread(() -> showAdminSection(finalIsAdmin));
                }
            } catch (Exception e) {
                runOnUiThread(() -> showAdminSection(false));
            }
        }).start();
    }

    private void showAdminSection(boolean isAdmin) {
        adminSection.removeAllViews();
        if (isAdmin) {
            adminSection.setVisibility(View.VISIBLE);
            // Card-like container
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
            card.setPadding(32, 32, 32, 32);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(0, 48, 0, 0);
            card.setLayoutParams(cardParams);
            card.setElevation(12f);

            // Heading
            TextView heading = new TextView(this);
            heading.setText("🛡️ Admin Panel");
            heading.setTextSize(20);
            heading.setTypeface(null, Typeface.BOLD);
            heading.setTextColor(Color.parseColor("#222222"));
            heading.setGravity(Gravity.CENTER);
            heading.setPadding(0, 0, 0, 24);
            card.addView(heading);

            // Buttons
            Button productsBtn = new Button(this);
            productsBtn.setText("Manage Products");
            styleAdminButton(productsBtn);
            productsBtn.setOnClickListener(v -> {
                startActivity(new android.content.Intent(this, AdminProductActivity.class));
            });

            Button ordersBtn = new Button(this);
            ordersBtn.setText("Manage Orders");
            styleAdminButton(ordersBtn);
            ordersBtn.setOnClickListener(v -> startActivity(new android.content.Intent(this, OrderActivity.class)));

            Button usersBtn = new Button(this);
            usersBtn.setText("Manage Users");
            styleAdminButton(usersBtn);
            usersBtn.setOnClickListener(v -> startActivity(new Intent(this, AdminUserActivity.class)));

            card.addView(productsBtn);
            card.addView(ordersBtn);
            card.addView(usersBtn);

            adminSection.addView(card);
        } else {
            adminSection.setVisibility(View.GONE);
        }
    }

    // Helper to style admin buttons
    private void styleAdminButton(Button btn) {
        btn.setAllCaps(true);
        btn.setTextSize(16);
        btn.setTypeface(null, Typeface.BOLD);
        btn.setTextColor(Color.WHITE);
        btn.setBackgroundResource(R.drawable.button_green_rounded);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 16, 0, 0);
        btn.setLayoutParams(params);
        btn.setPadding(0, 36, 0, 36);
    }
} 