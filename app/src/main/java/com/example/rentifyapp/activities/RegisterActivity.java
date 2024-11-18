package com.example.rentifyapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rentifyapp.R;
import com.example.rentifyapp.models.User;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText etFirstName, etLastName, etEmail, etPassword, etConfirmPassword;
    private RadioGroup rgRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase App and Auth
        FirebaseApp.initializeApp(this);
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

        // Assign 'role' using a ternary operator and declare it as final
        final String role = (selectedRoleId == R.id.rbLessor) ? "Lessor" :
                (selectedRoleId == R.id.rbRenter) ? "Renter" : "Unknown";

        // Validate inputs
        if (TextUtils.isEmpty(firstName)) {
            etFirstName.setError(getString(R.string.first_name_required));
            return;
        }

        if (TextUtils.isEmpty(lastName)) {
            etLastName.setError(getString(R.string.last_name_required));
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getString(R.string.email_required));
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.invalid_email));
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError(getString(R.string.password_required));
            return;
        }

        if (password.length() < 6) {
            etPassword.setError(getString(R.string.password_length_error));
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.passwords_do_not_match));
            return;
        }

        if ("Unknown".equals(role)) {
            Toast.makeText(this, getString(R.string.select_valid_role), Toast.LENGTH_SHORT).show();
            return;
        }

        // Register user with Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Create a user object to store in Firestore
                            User newUser = new User(firebaseUser.getUid(), firstName, lastName, email, role);

                            // Store user data in Firestore
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users").document(firebaseUser.getUid())
                                    .set(newUser)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegisterActivity.this, getString(R.string.registration_successful), Toast.LENGTH_SHORT).show();

                                        // Navigate to appropriate dashboard
                                        navigateToDashboard(firebaseUser);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(RegisterActivity.this, getString(R.string.error_saving_user_data) + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, getString(R.string.authentication_failed) + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
                                        Toast.makeText(RegisterActivity.this, getString(R.string.unknown_role, role), Toast.LENGTH_SHORT).show();
                                        break;
                                }
                                finish(); // Close RegisterActivity after navigation
                            } else {
                                Toast.makeText(RegisterActivity.this, getString(R.string.role_not_found), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, getString(R.string.user_data_not_found), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(RegisterActivity.this, getString(R.string.error_fetching_user_data, e.getMessage()), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
