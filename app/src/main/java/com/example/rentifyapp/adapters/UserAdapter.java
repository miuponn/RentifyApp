package com.example.rentifyapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rentifyapp.R;
import com.example.rentifyapp.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private Context context;
    private CollectionReference usersRef;

    public UserAdapter(List<User> userList, Context context, CollectionReference usersRef) {
        this.userList = userList;
        this.context = context;
        this.usersRef = usersRef;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvUserName.setText(user.getFirstName() + " " + user.getLastName());
        holder.tvUserEmail.setText(user.getEmail());
        holder.tvUserRole.setText(context.getString(R.string.role_format, user.getRole()));

        holder.btnDeleteUser.setOnClickListener(v ->
                new AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.delete_disable_user_title))
                        .setMessage(context.getString(R.string.delete_disable_user_message))
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            // Soft delete: Set isActive to false
                            usersRef.document(user.getUserId())
                                    .update("isActive", false)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, context.getString(R.string.user_disabled), Toast.LENGTH_SHORT).show();
                                        // Remove the user from the list
                                        userList.remove(position);
                                        notifyItemRemoved(position);
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(context, context.getString(R.string.error_disabling_user), Toast.LENGTH_SHORT).show()
                                    );
                        })
                        .setNegativeButton(R.string.no, null)
                        .show()
        );
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserEmail, tvUserRole;
        MaterialButton btnDeleteUser;

        UserViewHolder(View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            btnDeleteUser = itemView.findViewById(R.id.btnDeleteUser);
        }
    }
}
