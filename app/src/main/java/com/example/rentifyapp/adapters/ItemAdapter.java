// File: app/src/main/java/com/example/rentifyapp/adapters/ItemAdapter.java

package com.example.rentifyapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rentifyapp.R;
import com.example.rentifyapp.databinding.ItemLayoutBinding;
import com.example.rentifyapp.interfaces.OnItemDeleteListener;
import com.example.rentifyapp.interfaces.OnItemEditListener;
import com.example.rentifyapp.models.Item;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<Item> itemList;
    private Context context;
    private OnItemDeleteListener deleteListener;
    private OnItemEditListener editListener;

    public ItemAdapter(List<Item> itemList, Context context,
                       OnItemDeleteListener deleteListener,
                       OnItemEditListener editListener) {
        this.itemList = itemList;
        this.context = context;
        this.deleteListener = deleteListener;
        this.editListener = editListener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout using View Binding
        ItemLayoutBinding binding = ItemLayoutBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemList.get(position);
        holder.binding.tvItemName.setText(item.getName());
        holder.binding.tvItemDescription.setText(item.getDescription());
        holder.binding.tvItemFee.setText(context.getString(R.string.fee_colon, item.getFee()));
        holder.binding.tvItemTimePeriod.setText(context.getString(R.string.rent_period_colon, item.getTimePeriod()));
        holder.binding.tvItemCategory.setText(context.getString(R.string.category_colon, item.getCategoryId()));

        // Load image if available
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            holder.binding.ivItemImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder_image)
                    .into(holder.binding.ivItemImage);
        } else {
            holder.binding.ivItemImage.setVisibility(View.GONE);
        }

        // Edit Button Click Listener
        holder.binding.btnEditItem.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onItemEdit(item);
            }
        });

        // Delete Button Click Listener
        holder.binding.btnDeleteItem.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onItemDelete(item.getItemId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // ViewHolder Class using View Binding
    public class ItemViewHolder extends RecyclerView.ViewHolder {
        ItemLayoutBinding binding;

        public ItemViewHolder(@NonNull ItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
