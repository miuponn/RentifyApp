package com.example.rentifyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
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

        // Placeholder for button actions (Users, Categories, Moderation, Analytics)
        findViewById(R.id.card_users).setOnClickListener(v -> {
            Toast.makeText(this, "User Management clicked", Toast.LENGTH_SHORT).show();
            // You can navigate to User Management activity here
        });

        findViewById(R.id.card_categories).setOnClickListener(v -> {
            Toast.makeText(this, "Category Management clicked", Toast.LENGTH_SHORT).show();
            // You can navigate to Category Management activity here
        });

        findViewById(R.id.card_moderation).setOnClickListener(v -> {
            Toast.makeText(this, "Content Moderation clicked", Toast.LENGTH_SHORT).show();
            // You can navigate to Content Moderation activity here
        });

        findViewById(R.id.card_analytics).setOnClickListener(v -> {
            Toast.makeText(this, "Analytics clicked", Toast.LENGTH_SHORT).show();
            // You can navigate to Analytics activity here
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
                            tvName.setText("Hi, " + firstName + "!");
                        }
                        if ("Admin".equals(role)) {
                            tvGreeting.setText("Admin Dashboard");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading admin data", Toast.LENGTH_SHORT).show();
                });
    }
}

