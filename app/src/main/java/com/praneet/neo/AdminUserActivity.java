package com.praneet.neo;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.view.ViewGroup;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class AdminUserActivity extends AppCompatActivity {
    private TextView userCountText;
    private LinearLayout userListLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user);
        userCountText = findViewById(R.id.text_user_count);
        userListLayout = findViewById(R.id.layout_user_list);
        fetchUserCount();
        fetchUserNames();
    }

    private void fetchUserCount() {
        SupabaseManager.fetchUserCount(new SupabaseManager.UserCountCallback() {
            @Override
            public void onSuccess(final int count) {
                runOnUiThread(() -> userCountText.setText("Total Users: " + count));
            }
            @Override
            public void onError(final String error) {
                runOnUiThread(() -> {
                    userCountText.setText("Error fetching user count");
                    Toast.makeText(AdminUserActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void fetchUserNames() {
        SupabaseManager.fetchAllUserNames(new SupabaseManager.UserNamesCallback() {
            @Override
            public void onSuccess(final List<String> names) {
                runOnUiThread(() -> displayUserNames(names));
            }
            @Override
            public void onError(final String error) {
                runOnUiThread(() -> Toast.makeText(AdminUserActivity.this, error, Toast.LENGTH_LONG).show());
            }
        });
    }

    private void displayUserNames(List<String> names) {
        userListLayout.removeAllViews();
        for (String name : names) {
            TextView tv = new TextView(this);
            tv.setText("• " + name);
            tv.setTextSize(18);
            tv.setPadding(8, 8, 8, 8);
            userListLayout.addView(tv);
        }
    }
} 