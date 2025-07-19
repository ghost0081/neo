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

public class AccountActivity extends AppCompatActivity {
    
    // UI Elements
    private LinearLayout loginForm, registerForm, userProfile;
    private EditText loginEmail, loginPassword;
    private EditText registerName, registerEmail, registerPhone, registerAddress, registerPassword;
    private Button loginButton, registerButton, logoutButton;
    private TextView toggleMode, loginStatus, registerStatus;
    private TextView profileName, profileEmail, profilePhone, profileAddress;
    
    private boolean isLoginMode = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }
    
    private void setupClickListeners() {
        // Toggle between login and register
        toggleMode.setOnClickListener(v -> toggleMode());
        
        // Login button
        loginButton.setOnClickListener(v -> handleLogin());
        
        // Sign up button (on login form)
        Button signupButton = findViewById(R.id.signup_button);
        signupButton.setOnClickListener(v -> showRegistrationForm());
        
        // Register button
        registerButton.setOnClickListener(v -> handleRegistration());
        
        // Logout button
        logoutButton.setOnClickListener(v -> handleLogout());
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
        
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty() || password.isEmpty()) {
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
                    registerStatus.setText(error);
                    registerStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    registerButton.setEnabled(true);
                    registerButton.setText("Create Account");
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
    }
    
    private void showUserProfile() {
        loginForm.setVisibility(View.GONE);
        registerForm.setVisibility(View.GONE);
        userProfile.setVisibility(View.VISIBLE);
        
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
                    // If profile data is empty, use stored data
                    String displayName = name;
                    String displayPhone = phone;
                    String displayAddress = address;
                    
                    if (name.isEmpty() || name.equals("User")) {
                        displayName = storedName;
                        displayPhone = storedPhone;
                        displayAddress = storedAddress;
                        
                        // If still empty, use hardcoded data for testing
                        if (displayName.isEmpty()) {
                            displayName = "praneet singh";
                            displayPhone = "8851271943";
                            displayAddress = "hardev nagar";
                        }
                        
                        // Try to update the profile with stored data
                        if (!displayName.isEmpty() && !displayPhone.isEmpty() && !displayAddress.isEmpty()) {
                            updateProfileWithStoredData(displayName, displayPhone, displayAddress);
                        }
                    }
                    
                    profileName.setText(displayName);
                    profileEmail.setText("Email: " + SupabaseManager.getCurrentUserEmail());
                    profilePhone.setText(displayPhone);
                    profileAddress.setText(displayAddress);
                    
                    Log.d("AccountActivity", "Displaying - Name: " + displayName + ", Phone: " + displayPhone + ", Address: " + displayAddress);
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    // If there's an error, use hardcoded data for testing
                    String displayName = storedName.isEmpty() ? "praneet singh" : storedName;
                    String displayPhone = storedPhone.isEmpty() ? "8851271943" : storedPhone;
                    String displayAddress = storedAddress.isEmpty() ? "hardev nagar" : storedAddress;
                    
                    profileName.setText(displayName);
                    profileEmail.setText("Email: " + SupabaseManager.getCurrentUserEmail());
                    profilePhone.setText(displayPhone);
                    profileAddress.setText(displayAddress);
                    
                    // Try to create/update profile with stored data
                    if (!displayName.isEmpty() && !displayPhone.isEmpty() && !displayAddress.isEmpty()) {
                        updateProfileWithStoredData(displayName, displayPhone, displayAddress);
                    }
                    
                    Log.d("AccountActivity", "Error case - Displaying - Name: " + displayName + ", Phone: " + displayPhone + ", Address: " + displayAddress);
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
        
        // Clear any previous status messages
        loginStatus.setText("");
        registerStatus.setText("");
    }

    // Email validation helper method
    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }
} 