package com.example.rentifyapp.models;

public class Category {
    private String categoryId;
    private String name;
    private String description;

    // Empty constructor needed for Firestore
    public Category() {}

    // Parameterized constructor
    public Category(String categoryId, String name, String description) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
