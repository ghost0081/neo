package com.praneet.neo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class FavoriteActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Hide action bar if it exists
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        setContentView(R.layout.activity_favorite);
    }
} 