package com.chakulaconnect;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.squareup.picasso.Picasso;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostFragment extends Fragment {

    RecyclerView postRecycleView;
    ArrayList<DonationModel> donationModels;
    FloatingActionButton fabDonateRequest;
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
        LinearLayoutManager layoutActivityManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        postRecycleView.setLayoutManager(layoutActivityManager);
        donationModels = new ArrayList<>();
        DonationsAdapter donationsAdapter = new DonationsAdapter(getContext(), donationModels);
        postRecycleView.setAdapter(donationsAdapter);

        fabDonateRequest.setOnClickListener(v->{
            Intent donRecIntent = new Intent(getContext(), ActivityDonationInfo.class);
            donRecIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(donRecIntent);
        });

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Donations").get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if(task.isSuccessful()){
                            for(DataSnapshot snapshot : task.getResult().getChildren()){
                                if(snapshot.exists()){
                                    donationModels.add(snapshot.getValue(DonationModel.class));
                                }
                            }
                            donationsAdapter.notifyDataSetChanged();
                        }
                    }
                }).addOnFailureListener(e->{
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        return view;
    }
}
class DonationsAdapter extends RecyclerView.Adapter<DonationsAdapter.DonationHolder>{
    Context context;
    ArrayList<DonationModel> donationModels;

    public DonationsAdapter(Context context, ArrayList<DonationModel> donationModels) {
        this.context = context;
        this.donationModels = donationModels;
    }

    @NonNull
    @Override
    public DonationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.donation_single_layout, parent, false);
        return new DonationHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DonationHolder holder, int position) {
        DonationModel donationModel = donationModels.get(position);
        holder.txtFoodType.setText("Food type: ".concat(donationModel.getFoodDetails().get("foodType").toString()));
        holder.txtFoodQuantity.setText("Food quantity: ".concat(donationModel.getFoodDetails().get("quantity")+"KGs"));

        Instant instant = Instant.ofEpochMilli(Long.parseLong(donationModel.getDonationDate()));
        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        holder.txtDonationDateSingle.setText(localDateTime.format(formatter));

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Users").child(donationModel.getDonor()).get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()){
                            DataSnapshot snapshot = task.getResult();
                            if(snapshot.exists()){
                                UserModel userModel = snapshot.getValue(UserModel.class);
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                holder.userName.setText( userModel.getAccountDetails().get("userId").equals(user.getUid())
                                        ? "You donated" : userModel.getAccountDetails().get("userName").concat(" donated"));
                                Picasso.get().load(userModel.getAccountDetails().get("imageUri"))
                                        .into(holder.civUserDonor);
                            }
                        }
                    }
                }).addOnFailureListener(e->{
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return donationModels.size();
    }

    class DonationHolder extends RecyclerView.ViewHolder{
        CircleImageView civUserDonor;
        TextView userName, txtFoodType, txtFoodQuantity, txtDonationDateSingle;
        public DonationHolder(@NonNull View itemView) {
            super(itemView);
            civUserDonor = itemView.findViewById(R.id.civUserDonorImage);
            userName = itemView.findViewById(R.id.txtUserNameDonor);
            txtFoodType = itemView.findViewById(R.id.txtFoodTypeDonor);
            txtFoodQuantity = itemView.findViewById(R.id.txtFoodQuantityDonor);
            txtDonationDateSingle = itemView.findViewById(R.id.txtDonationDate);
        }
    }
}