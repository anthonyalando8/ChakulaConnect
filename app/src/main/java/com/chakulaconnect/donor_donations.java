package com.chakulaconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class donor_donations extends AppCompatActivity implements FirebaseAuth.AuthStateListener {
    CircleImageView civUserDonorImage;
    TextView txtUserName, txtAllDonations, txtTotalDonations;
    RecyclerView rvUserDonations;
    ArrayList<DonationModel> donationModels;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference donationRef;
    StorageReference donationStorageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_donations);
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("My donations");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        donationRef = FirebaseDatabase.getInstance().getReference();
        donationStorageRef = FirebaseStorage.getInstance().getReference();

        civUserDonorImage = findViewById(R.id.civUserImageDonations);
        txtUserName = findViewById(R.id.userNameDonations);
        txtAllDonations = findViewById(R.id.txt_btn_SeeAll);
        txtTotalDonations = findViewById(R.id.txtTotalDonations);
        rvUserDonations = findViewById(R.id.rvUserDonations);

        donationModels = new ArrayList<>();
        UserDonationsAdapter userDonationsAdapter = new UserDonationsAdapter(this, donationModels);
        LinearLayoutManager layoutActivityManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvUserDonations.setLayoutManager(layoutActivityManager);
        rvUserDonations.setAdapter(userDonationsAdapter);

        if(isUser()){
            UserModel donorRecModel;
            Gson gson = new Gson();
            SharedPreferences sharedPreferences = getSharedPreferences(user.getUid()+"_pref", MODE_PRIVATE);
            String userData = sharedPreferences.getString(user.getUid()+"_data", null);
            if(userData != null){
                donorRecModel = gson.fromJson(userData, UserModel.class);
                txtUserName.setText("Hello, welcome back "+donorRecModel.getAccountDetails().get("userName"));
                Picasso.get().load(donorRecModel.getAccountDetails().get("imageUri")).into(civUserDonorImage);
                if(donorRecModel.getAccount_role().containsKey("Donor")){
                    donationRef.child("Donations").get()
                            .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if(task.isSuccessful()){
                                        for (DataSnapshot snapshot : task.getResult().getChildren()){
                                            if(snapshot.exists()){
                                                if(snapshot.child("donor").getValue(String.class).equals(user.getUid())){
                                                    donationModels.add(snapshot.getValue(DonationModel.class));
                                                }
                                                userDonationsAdapter.notifyDataSetChanged();
                                            }
                                        }
                                        txtTotalDonations.setText( donorRecModel.getAccount_role().containsKey("Donor") ?
                                                "Total donations ("+ donationModels.size()+ ")" : "Total request ("+ donationModels.size()+ ")");
                                    }
                                }
                            }).addOnFailureListener(e->{
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }

        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isUser(){
        if(user != null){
            return true;
        }
        return false;
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

    }
}
class UserDonationsAdapter extends RecyclerView.Adapter<UserDonationsAdapter.AdapterHolder>{

    Context context;
    ArrayList<DonationModel> donationModels;

    public UserDonationsAdapter(Context context, ArrayList<DonationModel> donationModels) {
        this.context = context;
        this.donationModels = donationModels;
    }

    @NonNull
    @Override
    public AdapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_donation_single_layout, parent, false);
        return new AdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterHolder holder, int position) {
        String [] foodType = context.getResources().getStringArray(R.array.food_type_donor_array);
        DonationModel donationModel = donationModels.get(position);
        holder.txtDonationFoodType.setText("Donated ".concat(donationModel.getFoodDetails().get("foodType").toString()));
        Instant instant = Instant.ofEpochMilli(Long.parseLong(donationModel.getDonationDate()));
        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        holder.txtDonationDate.setText(localDateTime.format(formatter));
        if (donationModel.getFoodDetails().get("foodType").equals(foodType[1])){
            holder.civDonationIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),R.drawable.cooked_meals, context.getTheme()));
        } else if (donationModel.getFoodDetails().get("foodType").equals(foodType[2])) {
            holder.civDonationIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),R.drawable.delicate, context.getTheme()));
        } else if (donationModel.getFoodDetails().get("foodType").equals(foodType[3])) {
            holder.civDonationIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),R.drawable.donations, context.getTheme()));
        } else if (donationModel.getFoodDetails().get("foodType").equals(foodType[4])) {
            holder.civDonationIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),R.drawable.fruits, context.getTheme()));
        } else if (donationModel.getFoodDetails().get("foodType").equals(foodType[5])) {
            holder.civDonationIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),R.drawable.vegetables, context.getTheme()));
        }
    }

    @Override
    public int getItemCount() {
        return donationModels.size();
    }

    class AdapterHolder extends RecyclerView.ViewHolder{
        CircleImageView civDonationIcon;
        ConstraintLayout mainLayout;
        TextView txtDonationFoodType, txtDonationDate;
        public AdapterHolder(@NonNull View itemView) {
            super(itemView);
            civDonationIcon = itemView.findViewById(R.id.civDonationIcon);
            mainLayout = itemView.findViewById(R.id.clMainLayout);
            txtDonationFoodType = itemView.findViewById(R.id.txtDonationType);
            txtDonationDate = itemView.findViewById(R.id.txtDonationDateSingle);
        }
    }
}