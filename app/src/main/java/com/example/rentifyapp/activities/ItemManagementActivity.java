package com.example.rentifyapp.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rentifyapp.R;
import com.example.rentifyapp.adapters.ItemAdapter;
import com.example.rentifyapp.databinding.DialogAddEditItemBinding;
import com.example.rentifyapp.interfaces.OnItemDeleteListener;
import com.example.rentifyapp.interfaces.OnItemEditListener;
import com.example.rentifyapp.models.Item;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ItemManagementActivity extends AppCompatActivity implements OnItemDeleteListener, OnItemEditListener {

    private RecyclerView rvItems;
    private MaterialButton btnAddItem;
    private ItemAdapter itemAdapter;
    private List<Item> itemList;
    private FirebaseFirestore db;
    private CollectionReference itemsRef;
    private ListenerRegistration itemsListener;

    // View Binding for Dialog
    private DialogAddEditItemBinding dialogBinding;

    // Firebase Storage Reference for Images
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private Uri selectedImageUri;

    // Permission Request Code
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize View Binding for Activity
        com.example.rentifyapp.databinding.ActivityItemManagementBinding binding =
                com.example.rentifyapp.databinding.ActivityItemManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Views using View Binding
        rvItems = binding.rvItems; // Ensure rvItems exists in activity_item_management.xml
        btnAddItem = binding.btnAddItem;

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        itemsRef = db.collection("items");

        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Initialize item list and adapter
        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(itemList, this, this, this); // 'this' refers to both listeners

        // Setup RecyclerView
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(itemAdapter);

        // Load items from Firestore
        loadItems();

        // Add Item Button Click Listener
        btnAddItem.setOnClickListener(v -> showAddEditItemDialog(null));
    }

    // Method to load items from Firestore
    private void loadItems() {
        itemsListener = itemsRef.whereEqualTo("isActive", true) // Assuming an 'isActive' flag
                .addSnapshotListener((QuerySnapshot value, FirebaseFirestoreException error) -> {
                    if (error != null) {
                        Log.e("ItemManagement", "Error loading items", error);
                        Toast.makeText(ItemManagementActivity.this, getString(R.string.error_loading_items), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        itemList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Item item = doc.toObject(Item.class);
                            if (item != null) {
                                item.setItemId(doc.getId());
                                itemList.add(item);
                            }
                        }
                        itemAdapter.notifyDataSetChanged();
                    }
                });
    }

    // Method to show Add/Edit Item Dialog with validation
    private void showAddEditItemDialog(Item item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(item == null ? getString(R.string.add_item) : getString(R.string.edit_item));

        // Inflate the dialog layout using View Binding
        dialogBinding = DialogAddEditItemBinding.inflate(LayoutInflater.from(this));
        builder.setView(dialogBinding.getRoot());

        // Populate fields if editing
        if (item != null) {
            dialogBinding.etItemName.setText(item.getName());
            dialogBinding.etItemDescription.setText(item.getDescription());
            dialogBinding.etItemFee.setText(String.valueOf(item.getFee()));
            dialogBinding.etItemTimePeriod.setText(item.getTimePeriod());
            dialogBinding.etItemCategory.setText(item.getCategoryId());

            // Load existing image if available
            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                dialogBinding.ivSelectedImage.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(item.getImageUrl())
                        .into(dialogBinding.ivSelectedImage);
            }
        }

        // Upload Photo Button Click Listener
        dialogBinding.btnUploadPhoto.setOnClickListener(v -> {
            // Check for READ_EXTERNAL_STORAGE permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_READ_EXTERNAL_STORAGE);
            } else {
                openImagePicker();
            }
        });

        builder.setPositiveButton(getString(R.string.save), null); // Set null to override later
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        // Override the Save button to prevent automatic dismissal
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (validateInputs()) {
                    if (item == null) {
                        // Add new item
                        addItem();
                    } else {
                        // Edit existing item
                        editItem(item.getItemId());
                    }
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    // Method to validate inputs
    private boolean validateInputs() {
        boolean isValid = true;

        String name = dialogBinding.etItemName.getText().toString().trim();
        String description = dialogBinding.etItemDescription.getText().toString().trim();
        String feeStr = dialogBinding.etItemFee.getText().toString().trim();
        String timePeriod = dialogBinding.etItemTimePeriod.getText().toString().trim();
        String category = dialogBinding.etItemCategory.getText().toString().trim();

        // Validate Name
        if (name.isEmpty()) {
            dialogBinding.tilItemName.setError(getString(R.string.name_required));
            isValid = false;
        } else {
            dialogBinding.tilItemName.setError(null);
        }

        // Validate Description
        if (description.isEmpty()) {
            dialogBinding.tilItemDescription.setError(getString(R.string.description_required));
            isValid = false;
        } else {
            dialogBinding.tilItemDescription.setError(null);
        }

        // Validate Fee
        double fee = 0.0;
        if (feeStr.isEmpty()) {
            dialogBinding.tilItemFee.setError(getString(R.string.fee_required));
            isValid = false;
        } else {
            try {
                fee = Double.parseDouble(feeStr);
                if (fee <= 0) {
                    dialogBinding.tilItemFee.setError(getString(R.string.fee_positive));
                    isValid = false;
                } else {
                    dialogBinding.tilItemFee.setError(null);
                }
            } catch (NumberFormatException e) {
                dialogBinding.tilItemFee.setError(getString(R.string.fee_invalid));
                isValid = false;
            }
        }

        // Validate Time Period
        if (timePeriod.isEmpty()) {
            dialogBinding.tilItemTimePeriod.setError(getString(R.string.time_period_required));
            isValid = false;
        } else {
            dialogBinding.tilItemTimePeriod.setError(null);
        }

        // Validate Category
        if (category.isEmpty()) {
            dialogBinding.tilItemCategory.setError(getString(R.string.select_valid_category));
            isValid = false;
        } else {
            dialogBinding.tilItemCategory.setError(null);
        }

        return isValid;
    }

    // Method to add a new item to Firestore
    private void addItem() {
        String name = dialogBinding.etItemName.getText().toString().trim();
        String description = dialogBinding.etItemDescription.getText().toString().trim();
        double fee = Double.parseDouble(dialogBinding.etItemFee.getText().toString().trim());
        String timePeriod = dialogBinding.etItemTimePeriod.getText().toString().trim();
        String category = dialogBinding.etItemCategory.getText().toString().trim();

        // Create a new Item object
        Item newItem = new Item();
        newItem.setName(name);
        newItem.setDescription(description);
        newItem.setFee(fee);
        newItem.setTimePeriod(timePeriod);
        newItem.setCategoryId(category);
        newItem.setIsActive(true);

        // Handle Image Upload if an image is selected
        if (selectedImageUri != null) {
            String imagePath = "images/" + System.currentTimeMillis() + "_" + getFileName(selectedImageUri);
            StorageReference imageRef = storageRef.child(imagePath);

            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                newItem.setImageUrl(uri.toString());
                                saveItemToFirestore(newItem);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("ItemManagement", "Error getting download URL", e);
                                Toast.makeText(this, getString(R.string.error_saving_item_data) + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }))
                    .addOnFailureListener(e -> {
                        Log.e("ItemManagement", "Error uploading image", e);
                        Toast.makeText(this, getString(R.string.error_saving_item_data) + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // No image selected, proceed to save
            saveItemToFirestore(newItem);
        }
    }

    // Helper method to get file name from Uri
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(android.provider.OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    // Method to save Item to Firestore
    private void saveItemToFirestore(Item item) {
        itemsRef.add(item)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, getString(R.string.item_added), Toast.LENGTH_SHORT).show();
                    // Optionally, refresh the item list
                    loadItems();
                })
                .addOnFailureListener(e -> {
                    Log.e("ItemManagement", "Error adding item", e);
                    Toast.makeText(this, getString(R.string.error_saving_item_data) + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Method to edit an existing item in Firestore
    private void editItem(String itemId) {
        String name = dialogBinding.etItemName.getText().toString().trim();
        String description = dialogBinding.etItemDescription.getText().toString().trim();
        double fee = Double.parseDouble(dialogBinding.etItemFee.getText().toString().trim());
        String timePeriod = dialogBinding.etItemTimePeriod.getText().toString().trim();
        String category = dialogBinding.etItemCategory.getText().toString().trim();

        // Create a map with updated fields
        Item updatedItem = new Item();
        updatedItem.setName(name);
        updatedItem.setDescription(description);
        updatedItem.setFee(fee);
        updatedItem.setTimePeriod(timePeriod);
        updatedItem.setCategoryId(category);
        updatedItem.setIsActive(true); // Ensure the item remains active

        // Handle Image Upload if a new image is selected
        if (selectedImageUri != null) {
            String imagePath = "images/" + System.currentTimeMillis() + "_" + getFileName(selectedImageUri);
            StorageReference imageRef = storageRef.child(imagePath);

            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                updatedItem.setImageUrl(uri.toString());
                                updateItemInFirestore(itemId, updatedItem);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("ItemManagement", "Error getting download URL", e);
                                Toast.makeText(this, getString(R.string.error_saving_item_data) + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }))
                    .addOnFailureListener(e -> {
                        Log.e("ItemManagement", "Error uploading image", e);
                        Toast.makeText(this, getString(R.string.error_saving_item_data) + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // No new image selected, proceed to update
            updateItemInFirestore(itemId, updatedItem);
        }
    }

    // Method to update Item in Firestore
    private void updateItemInFirestore(String itemId, Item item) {
        itemsRef.document(itemId)
                .set(item)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, getString(R.string.item_updated), Toast.LENGTH_SHORT).show();
                    // Optionally, refresh the item list
                    loadItems();
                })
                .addOnFailureListener(e -> {
                    Log.e("ItemManagement", "Error updating item", e);
                    Toast.makeText(this, getString(R.string.error_saving_item_data) + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Method to open image picker
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_READ_EXTERNAL_STORAGE);
    }

    @Override
    public void onItemDelete(String itemId) {
        // Soft delete: Set isActive to false
        itemsRef.document(itemId)
                .update("isActive", false)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, getString(R.string.item_deleted), Toast.LENGTH_SHORT).show();
                    // Refresh the item list
                    loadItems();
                })
                .addOnFailureListener(e -> {
                    Log.e("ItemManagement", "Error deleting item", e);
                    Toast.makeText(this, getString(R.string.error_deleting_item), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onItemEdit(Item item) {
        // Show the Add/Edit Item Dialog with existing item data
        showAddEditItemDialog(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detach Firestore listener to prevent memory leaks
        if (itemsListener != null) {
            itemsListener.remove();
        }
    }

    // Handle the result from image picker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the result is from image picker
        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                selectedImageUri = imageUri;
                dialogBinding.ivSelectedImage.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(imageUri)
                        .into(dialogBinding.ivSelectedImage);
            }
        }
    }

    // Handle permission request results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission denied to access your External storage.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
