package com.example.rentifyapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rentifyapp.R;
import com.example.rentifyapp.adapters.UserAdapter;
import com.example.rentifyapp.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserManagementActivity extends AppCompatActivity {

    private RecyclerView rvUsers;
    private MaterialButton btnAddUser;
    private UserAdapter userAdapter;
    private List<User> userList;
    private FirebaseFirestore db;
    private CollectionReference usersRef;
    private ListenerRegistration usersListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        // Initialize UI elements
        rvUsers = findViewById(R.id.rvUsers);
        btnAddUser = findViewById(R.id.btnAddUser);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");

        // Initialize user list and adapter
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, this, usersRef); // Pass context and usersRef

        // Setup RecyclerView
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(userAdapter);

        // Load users from Firestore
        loadUsers();

        // Add User Button Click Listener
        btnAddUser.setOnClickListener(v -> showAddEditUserDialog(null));
    }

    // Method to load users from Firestore
    private void loadUsers() {
        usersListener = usersRef.whereEqualTo("isActive", true)
                .addSnapshotListener((QuerySnapshot value, FirebaseFirestoreException error) -> {
                    if (error != null) {
                        Log.e("UserManagement", "Error loading users", error);
                        Toast.makeText(UserManagementActivity.this, getString(R.string.error_loading_users), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        userList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            User user = doc.toObject(User.class);
                            if (user != null) {
                                user.setUserId(doc.getId());
                                userList.add(user);
                            }
                        }
                        userAdapter.notifyDataSetChanged();
                    }
                });
    }

    // Method to show Add/Edit User Dialog
    private void showAddEditUserDialog(User user) {
        // Implementation for showing dialog
        // For example, you might open a dialog fragment to add or edit a user
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detach Firestore listener to prevent memory leaks
        if (usersListener != null) {
            usersListener.remove();
        }
    }
}
