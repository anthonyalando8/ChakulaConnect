package com.chakulaconnect;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostFragment extends Fragment {

    RecyclerView postRecycleView;
    ArrayList<PostModel> postModels;
    FloatingActionButton fabDonateRequest;
    SwipeRefreshLayout swipeRefreshLayout;
    public PostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        postRecycleView = view.findViewById(R.id.rcDonations);
        fabDonateRequest = view.findViewById(R.id.fabDonateRequest);
        swipeRefreshLayout = view.findViewById(R.id.refresh);

        LinearLayoutManager layoutActivityManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        postRecycleView.setLayoutManager(layoutActivityManager);
        postModels = new ArrayList<>();
        PostAdapter postAdapter = new PostAdapter(getContext(), postModels);
        postRecycleView.setAdapter(postAdapter);

        fabDonateRequest.setOnClickListener(v->{
            Intent donRecIntent = new Intent(getContext(), ActivityDonationInfo.class);
            donRecIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(donRecIntent);
        });

        loadPost(postAdapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadPost(postAdapter);
            }
        });

        return view;
    }
    private void loadPost(PostAdapter postAdapter){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Posts").get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if(task.isSuccessful()){
                            swipeRefreshLayout.setRefreshing(false);
                            postModels.clear();
                            for(DataSnapshot snapshot : task.getResult().getChildren()){
                                if(snapshot.exists()){
                                    postModels.add(snapshot.getValue(PostModel.class));
                                }
                            }
                            postAdapter.notifyDataSetChanged();
                        }
                    }
                }).addOnFailureListener(e->{
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
class PostAdapter extends RecyclerView.Adapter<PostAdapter.DonationHolder>{
    Context context;
    ArrayList<PostModel> postModels;

    public PostAdapter(Context context, ArrayList<PostModel> postModels) {
        this.context = context;
        this.postModels = postModels;
    }

    @NonNull
    @Override
    public DonationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.donation_single_layout, parent, false);
        return new DonationHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DonationHolder holder, int position) {
        PostModel postModel = postModels.get(position);

        Instant instant = Instant.ofEpochMilli(Long.parseLong(postModel.getTime()));
        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        holder.txtPostDate.setText(localDateTime.format(formatter));
        holder.txtPostDescriptionText.setText(postModel.getTextDescription());

        holder.civUserDonor.setOnClickListener(v->{
            AlertDialog alertDialog = Progress.createAlertDialog(context, "Loading...");
            alertDialog.show();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users/"+postModel.getSourceId());
            userRef.get().addOnCompleteListener(task->{
                if(task.isSuccessful()){
                    alertDialog.dismiss();
                    UserModel userModel = task.getResult().getValue(UserModel.class);
                    Gson gson = new Gson();
                    String userData = gson.toJson(userModel);
                    Intent profileIntent = new Intent(context, Profile.class);
                    profileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    profileIntent.putExtra("userData", userData);
                    context.startActivity(profileIntent);
                }
            }).addOnFailureListener(e->{
                alertDialog.dismiss();
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            });

        });

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Users").child(postModel.getSourceId()).get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()){
                            DataSnapshot snapshot = task.getResult();
                            if(snapshot.exists()){
                                UserModel userModel = snapshot.getValue(UserModel.class);
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                holder.userName.setText( postModel.getPostType() == 0 ?
                                        userModel.getAccountDetails().get("userId").equals(user.getUid()) ? "You donated" : userModel.getAccountDetails().get("userName").concat(" donated")
                                        : userModel.getAccountDetails().get("userId").equals(user.getUid()) ? "You requested" : userModel.getAccountDetails().get("userName").concat(" requested"));
                                Picasso.get().load(userModel.getAccountDetails().get("imageUri"))
                                        .into(holder.civUserDonor);
                            }
                        }
                    }
                })
                .addOnFailureListener(e->{
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        if(postModel.getImageUri() != null && !postModel.getImageUri().isEmpty()){
            holder.rvDonationPictures.setVisibility(View.VISIBLE);
            int spanCount = 1;
            if (postModel.getImageUri().size() >= 2){
                spanCount = 2;
            }
            GridLayoutManager gridLayoutManager = new GridLayoutManager(context, spanCount);
            //FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(context, FlexDirection.ROW, FlexWrap.WRAP);
            holder.rvDonationPictures.setLayoutManager(gridLayoutManager);
            PostPicturesAdapter PostPicturesAdapter = new PostPicturesAdapter(context, postModel.getImageUri());
            holder.rvDonationPictures.setAdapter(PostPicturesAdapter);
            PostPicturesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return postModels.size();
    }

    class DonationHolder extends RecyclerView.ViewHolder{
        CircleImageView civUserDonor;
        TextView userName, txtPostDescriptionText, txtPostLink, txtPostDate;
        RecyclerView rvDonationPictures;
        public DonationHolder(@NonNull View itemView) {
            super(itemView);
            civUserDonor = itemView.findViewById(R.id.civUserDonorImage);
            userName = itemView.findViewById(R.id.txtUserNameDonor);
            txtPostDescriptionText = itemView.findViewById(R.id.txtPostDescriptionText);
            txtPostLink = itemView.findViewById(R.id.txtFoodQuantityDonor);
            txtPostDate = itemView.findViewById(R.id.txtDonationDate);
            rvDonationPictures = itemView.findViewById(R.id.rvDonationPictures);
        }
    }
}

class PostPicturesAdapter extends RecyclerView.Adapter<PostPicturesAdapter.PicturesHolder>{
    Context context;
    HashMap<String, String> imageHash;
    ArrayList<String> imageKeys;

    public PostPicturesAdapter(Context context, HashMap<String, String> imageHash) {
        this.context = context;
        this.imageHash = imageHash;
        this.imageKeys = new ArrayList<>(imageHash.keySet());
    }

    @NonNull
    @Override
    public PicturesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_image_layout, parent, false);
        return new PicturesHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PicturesHolder holder, int position) {
        String key = imageKeys.get(position);
        String imageUri = imageHash.get(key);
        if(!imageUri.isEmpty()){
            Picasso.get().load(imageUri).into(holder.imageViewCustom);
        }
    }

    @Override
    public int getItemCount() {
        return imageHash.size();
    }

    class PicturesHolder extends RecyclerView.ViewHolder{
        ImageView imageViewCustom;
        public PicturesHolder(@NonNull View itemView) {
            super(itemView);
            imageViewCustom = itemView.findViewById(R.id.imageViewCustom);
        }
    }
}