package com.example.rentifyapp.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rentifyapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LessorDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvUserRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_renter_dashboard);

        // Initialize UI elements
        tvWelcome = findViewById(R.id.tvWelcome);
        tvUserRole = findViewById(R.id.tvUserRole);

        // Get current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            fetchUserData(user.getUid());
        }
    }

    private void fetchUserData(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String role = documentSnapshot.getString("role");

                        // Display user's first name and role
                        if (firstName != null) {
                            tvWelcome.setText("Hi, " + firstName);
                        }
                        if (role != null) {
                            tvUserRole.setText(role);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error if needed (e.g., show toast)
                });
    }
}