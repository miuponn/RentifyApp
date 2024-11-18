package com.example.rentifyapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rentifyapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvGreeting, tvName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize UI elements
        tvGreeting = findViewById(R.id.tvGreeting);
        tvName = findViewById(R.id.tvName);

        // Fetch the current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            fetchAdminData(user.getUid());
        }

        // Handle logout functionality
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(AdminDashboardActivity.this, LoginActivity.class));
            finish(); // Close admin dashboard after logout
        });

        // Navigate to User Management
        findViewById(R.id.card_users).setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, UserManagementActivity.class);
            startActivity(intent);
        });

        // Navigate to Category Management
        findViewById(R.id.card_categories).setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, CategoryManagementActivity.class);
            startActivity(intent);
        });

        // Content Moderation Click Listener
        findViewById(R.id.card_moderation).setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.content_moderation_clicked), Toast.LENGTH_SHORT).show();
            // Implement Content Moderation Activity
        });

        // Analytics Click Listener
        findViewById(R.id.card_analytics).setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.analytics_clicked), Toast.LENGTH_SHORT).show();
            // Implement Analytics Activity
        });
    }

    // Fetch the admin data from Firestore
    private void fetchAdminData(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String role = documentSnapshot.getString("role");

                        // Display admin's first name and role
                        if (firstName != null) {
                            tvName.setText(getString(R.string.greeting_name, firstName));
                        }
                        if ("Admin".equals(role)) {
                            tvGreeting.setText(getString(R.string.admin_dashboard));
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, getString(R.string.error_loading_admin_data), Toast.LENGTH_SHORT).show()
                );
    }
}
