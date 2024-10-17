package com.example.rentifyapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.CollectionReference;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText etFirstName, etLastName, etEmail, etPassword, etConfirmPassword;
    private RadioGroup rgRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);



        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        rgRole = findViewById(R.id.rgRole);

        // Register Button Click Listener
        findViewById(R.id.btnRegister).setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        // Get input values
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        int selectedRoleId = rgRole.getCheckedRadioButtonId();
        String role = "";
        if (selectedRoleId == R.id.rbLessor) {
            role = "Lessor";
        } else if (selectedRoleId == R.id.rbRenter) {
            role = "Renter";
        }

        // Validate inputs (as in your current method)

        // Register user with Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Create a user object to store in Firestore
                            User newUser = new User(user.getUid(), firstName, lastName, email, role);

                            // Store user data in Firestore
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users").document(user.getUid())
                                    .set(newUser)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                                        // Navigate to appropriate dashboard
                                        navigateToDashboard(user);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(RegisterActivity.this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // User Model
    public class User {
        private String userId;
        private String firstName;
        private String lastName;
        private String email;
        private String role;

        // Constructor
        public User(String userId, String firstName, String lastName, String email, String role) {
            this.userId = userId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.role = role;
        }
    }

    private void navigateToDashboard(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (user != null) {
            // Fetch user role from Firestore
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String role = documentSnapshot.getString("role");

                            if (role != null) {
                                switch (role) {
                                    case "Admin":
                                        startActivity(new Intent(RegisterActivity.this, AdminDashboardActivity.class));
                                        break;
                                    case "Lessor":
                                        startActivity(new Intent(RegisterActivity.this, LessorDashboardActivity.class));
                                        break;
                                    case "Renter":
                                        startActivity(new Intent(RegisterActivity.this, RenterDashboardActivity.class));
                                        break;
                                    default:
                                        Toast.makeText(RegisterActivity.this, "Unknown role: " + role, Toast.LENGTH_SHORT).show();
                                        break;
                                }
                                finish(); // Close LoginActivity after navigation
                            } else {
                                Toast.makeText(RegisterActivity.this, "Role not found for user", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(RegisterActivity.this, "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

}

