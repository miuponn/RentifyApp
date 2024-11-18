package com.example.rentifyapp.adapters;
import com.example.rentifyapp.activities.CategoryManagementActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.rentifyapp.R;
import com.example.rentifyapp.interfaces.OnCategoryDeleteListener;
import com.example.rentifyapp.models.Category;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import android.widget.TextView;
import android.widget.Toast;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categoryList;
    private Context context;
    private OnCategoryDeleteListener deleteListener;

    public CategoryAdapter(List<Category> categoryList, Context context, OnCategoryDeleteListener deleteListener) {
        this.categoryList = categoryList;
        this.context = context;
        this.deleteListener = deleteListener;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.tvName.setText(category.getName());
        holder.tvDescription.setText(category.getDescription());

        holder.btnEdit.setOnClickListener(v -> {
            if (context instanceof CategoryManagementActivity) {
                ((CategoryManagementActivity) context).showAddEditCategoryDialog(category);
            } else {
                Toast.makeText(context, context.getString(R.string.error_unexpected), Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            // Confirm deletion
            new android.app.AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.delete_category))
                    .setMessage(context.getString(R.string.delete_category_confirmation))
                    .setPositiveButton(context.getString(R.string.yes), (dialog, which) -> {
                        if (deleteListener != null) {
                            deleteListener.onCategoryDelete(category.getCategoryId());
                        } else {
                            Toast.makeText(context, context.getString(R.string.error_unexpected), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(context.getString(R.string.no), null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription;
        MaterialButton btnEdit, btnDelete;

        CategoryViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvDescription = itemView.findViewById(R.id.tvCategoryDescription);
            btnEdit = itemView.findViewById(R.id.btnEditCategory);
            btnDelete = itemView.findViewById(R.id.btnDeleteCategory);
        }
    }
}
