package com.example.rentifyapp.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rentifyapp.R;
import com.example.rentifyapp.adapters.CategoryAdapter;
import com.example.rentifyapp.interfaces.OnCategoryDeleteListener;
import com.example.rentifyapp.models.Category;
import com.google.android.material.button.MaterialButton;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

public class CategoryManagementActivity extends AppCompatActivity implements OnCategoryDeleteListener {

    RecyclerView rvCategories;
    MaterialButton btnAddCategory;
    CategoryAdapter categoryAdapter;
    List<Category> categoryList;
    FirebaseFirestore db;
    private CollectionReference categoriesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);

        // Initialize UI elements
        rvCategories = findViewById(R.id.rvCategories);
        btnAddCategory = findViewById(R.id.btnAddCategory);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        categoriesRef = db.collection("categories");

        // Initialize category list and adapter
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(categoryList, this, this); // 'this' as both Context and Listener

        // Setup RecyclerView
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        rvCategories.setAdapter(categoryAdapter);

        // Load categories from Firestore
        loadCategories();

        // Add Category Button Click Listener
        btnAddCategory.setOnClickListener(v -> showAddEditCategoryDialog(null));
    }

    // Method to load categories from Firestore
    public void loadCategories() {
        categoriesRef.addSnapshotListener((QuerySnapshot value, FirebaseFirestoreException error) -> {
            if (error != null) {
                Toast.makeText(CategoryManagementActivity.this, getString(R.string.error_loading_categories), Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                categoryList.clear();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Category category = doc.toObject(Category.class);
                    if (category != null) {
                        category.setCategoryId(doc.getId());
                        categoryList.add(category);
                    }
                }
                categoryAdapter.notifyDataSetChanged();
            }
        });

    }

    // Method to show Add/Edit Category Dialog
    public void showAddEditCategoryDialog(Category category) {
        // Implementation for showing dialog
        // For example, you might open a dialog fragment to add or edit a category
    }

    // Implement the onCategoryDelete method from the interface
    @Override
    public void onCategoryDelete(String categoryId) {
        deleteCategory(categoryId);
    }

    // Public method to delete a category from Firestore
    public void deleteCategory(String categoryId) {
        categoriesRef.document(categoryId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, getString(R.string.category_deleted), Toast.LENGTH_SHORT).show();
                    // Refresh the category list
                    loadCategories();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, getString(R.string.error_deleting_category), Toast.LENGTH_SHORT).show();
                });
    }
}
