package com.chakulaconnect;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {
    private final ArrayList<UserModel> userModels;
    private Context context;

    public UsersAdapter(ArrayList<UserModel> userModels, Context context) {
        this.userModels = userModels;
        this.context = context;
    }



    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_user_layout, parent, false);
        return new UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        UserModel userModel = userModels.get(position);
        Map<String, String> accountInfo = userModel.getAccountDetails();
        holder.txt_user_name.setText(accountInfo.get("userName"));
        String imageUri = accountInfo.get("imageUri");
        String userId = accountInfo.get("userId");

        Map<String, Boolean> account_role = userModel.getAccount_role();
        if(account_role != null && account_role.containsKey("Donor") && account_role.get("Donor")){
            holder.txt_users_role.setText("Donor");
            holder.txt_users_role.setTextColor(ResourcesCompat.getColor(holder.txt_users_role.getResources(), R.color.info, null));
        }else {
            holder.txt_users_role.setText("Recipient");
            holder.txt_users_role.setTextColor(ResourcesCompat.getColor(holder.txt_users_role.getResources(), R.color.success, null));
        }

        Picasso.get()
                .load(imageUri).into(holder.civ_user_image);
        holder.btn_user_connect.setOnClickListener(v->{
            Toast.makeText(context, userId, Toast.LENGTH_SHORT).show();
        });
        holder.txt_user_name.setOnClickListener(v->{
            Intent profileIntent = new Intent(context, Profile.class);
            profileIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            profileIntent.putExtra("userId", userId);
            profileIntent.putExtra("displayName", accountInfo.get("userName"));
            profileIntent.putExtra("email", accountInfo.get("email"));
            context.startActivity(profileIntent);
        });
    }

    @Override
    public int getItemCount() {
        return userModels.size();
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{
        CircleImageView civ_user_image;
        TextView txt_user_name, txt_users_role;
        MaterialButton btn_user_connect;
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            civ_user_image = itemView.findViewById(R.id.civ_users_image);
            txt_user_name = itemView.findViewById(R.id.txt_users_name);
            btn_user_connect = itemView.findViewById(R.id.btn_users_connect);
            txt_users_role = itemView.findViewById(R.id.txt_users_role);
        }
    }
}

