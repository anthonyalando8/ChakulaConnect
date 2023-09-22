package com.chakulaconnect;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity implements FirebaseAuth.AuthStateListener {
    CircleImageView civUserImage;
    TextView txtUserName, txtUserEmail, btnAccountDetails, btnPersonalInfo, txtRole, txtType, txtComplete, txtJoinDate;
    TextView txtPhone, txtAddress, txtCountry, txtRegion, txtMoreInfo;
    ConstraintLayout clAccDetails, clPersonInfo, clIsUser;
    FirebaseAuth auth;
    FirebaseUser user;
    Gson gson;
    DatabaseReference databaseReference;
    String userName, userData, userEmail, imageUri, userId, role, entityType;
    MaterialButton btn_create_post, btn_edit_profile;
    ImageButton ib_manage_profile;
    ImageView iv_user_cover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        gson = new Gson();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        civUserImage = findViewById(R.id.civUserImage);
        txtUserName = findViewById(R.id.txtUserName);
        txtUserEmail = findViewById(R.id.txtUserEmail);
        btnAccountDetails = findViewById(R.id.txtAccountDetails);
        btnPersonalInfo = findViewById(R.id.txtPersonalInfo);
        btn_create_post = findViewById(R.id.btn_create_post);
        btn_edit_profile = findViewById(R.id.btn_edit_profile);
        ib_manage_profile = findViewById(R.id.btn_manage_profile);
        iv_user_cover = findViewById(R.id.iv_user_cover_profile);

        txtRole = findViewById(R.id.txtRole);
        txtType = findViewById(R.id.txtType);
        txtComplete = findViewById(R.id.txtComplete);
        txtJoinDate = findViewById(R.id.txtJoinDate);

        clAccDetails = findViewById(R.id.layoutAccountDetails);
        clPersonInfo = findViewById(R.id.layoutPersonalInfo);
        clIsUser = findViewById(R.id.cl_isUser);
        txtPhone = findViewById(R.id.txtPhone);
        txtAddress = findViewById(R.id.txtAddress);
        txtCountry = findViewById(R.id.txtCountry);
        txtRegion = findViewById(R.id.txtRegion);
        txtMoreInfo = findViewById(R.id.txtMoreInfo);

        userData = getIntent().getStringExtra("userData");
        if (isUser()){
            if(userData != null && !userData.isEmpty()){
                UserModel userModel = gson.fromJson(userData, UserModel.class);
                userId = userModel.getAccountDetails().get("userId");
                userName = userModel.getAccountDetails().get("userName");
                userEmail = userModel.getAccountDetails().get("email");
                imageUri = userModel.getAccountDetails().get("imageUri");

                if(userId.equals(user.getUid())) {
                    if(getSupportActionBar() !=  null){
                        getSupportActionBar().setTitle("My profile");
                    }
                    clIsUser.setVisibility(View.VISIBLE);

                    btn_create_post.setText(userModel.getAccount_role().containsKey("Donor") ? "Donate" : "Make request");

                }else{
                    if(getSupportActionBar() !=  null){
                        getSupportActionBar().setTitle(userName);
                        getSupportActionBar().setSubtitle(userEmail);
                    }
                    clIsUser.setVisibility(View.GONE);
                }

                if(userModel.getAccount_role().containsKey("Donor")){
                    role = "Donor";
                }else if(userModel.getAccount_role().containsKey("Recipient")){
                    role = "Recipient";
                }
                if(userModel.getAccount_role().containsKey("Organisation")){
                    entityType = "Organisation";
                }else if(userModel.getAccount_role().containsKey("Individual")){
                    entityType = "Individual";
                }
                txtUserEmail.setText(userEmail);
                txtUserName.setText(userName);
                txtPhone.setText(userModel.getMoreInfo().get("phone").toString());
                txtMoreInfo.setText(userModel.getMoreInfo().get("moreInfo").toString());

                if(userModel.getAccountDetails().containsKey("coverUri") && !userModel.getAccountDetails().get("coverUri").isEmpty()){
                    Picasso.get()
                            .load(userModel.getAccountDetails().get("coverUri"))
                            .into(iv_user_cover);
                }
                Picasso.get().load(imageUri).into(civUserImage);

                txtType.setText(entityType);
                txtRole.setText(role);

                txtComplete.setText(userModel.getAccount_role().get("complete").toString());

                txtJoinDate.setText(TimeCalculator.calculateTime(Long.parseLong(userModel.getAccountDetails().get("joinDate"))));

                retrievePersonalInfo(userId);
            }

        }
        btn_create_post.setOnClickListener(v->{
            Intent donateRequest = new Intent(this, ActivityDonationInfo.class);
            donateRequest.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(donateRequest);
        });
        btnAccountDetails.setOnClickListener(v->{
            animateLayoutChange(clPersonInfo, clAccDetails);
        });
        btnPersonalInfo.setOnClickListener(v->{
            animateLayoutChange(clAccDetails, clPersonInfo);
        });
        btn_edit_profile.setOnClickListener(v->{
            Intent editProfileIntent = new Intent(this, EditProfile.class);
            editProfileIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(editProfileIntent);
        });

    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if(firebaseAuth.getCurrentUser() == null){
            startActivity(new Intent(this, AuthLogin.class));
            finish();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Handle the up button click (e.g., navigate back)
                onBackPressed();
                return true;
            // Add more cases for other menu items if needed
        }
        return super.onOptionsItemSelected(item);
    }
    private boolean isUser(){
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            return  true;
        }
        return false;
    }

    private void retrievePersonalInfo(String userId){
        AlertDialog alertDialog = Progress.createAlertDialog(this, "Please wait");
        alertDialog.show();

        databaseReference.child("Users").child(userId)
                .get().addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        alertDialog.dismiss();
                        DataSnapshot snapshot = task.getResult();

                        if(snapshot.child("moreInfo").child("location").hasChildren()){
                            LocationModel locationModel = snapshot.child("moreInfo").child("location").getValue(LocationModel.class);
                            txtAddress.setText(locationModel.getStreetAddress());
                            txtCountry.setText(locationModel.getCountry());
                            txtRegion.setText(locationModel.getCounty());
                        }
                    }
                }).addOnFailureListener(e -> {
                    alertDialog.dismiss();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void animateLayoutChange(View layoutToHide, View layoutToShow) {
        layoutToHide.animate().alpha(0.0f).setDuration(300)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        if(layoutToShow == clAccDetails){
                            btnAccountDetails.setTypeface(null, Typeface.BOLD);
                            btnPersonalInfo.setTypeface(null, Typeface.NORMAL);
                        }else {
                            btnPersonalInfo.setTypeface(null, Typeface.BOLD);
                            btnAccountDetails.setTypeface(null, Typeface.NORMAL);
                        }
                        layoutToHide.setVisibility(View.GONE);
                        layoutToShow.setAlpha(0.0f);
                        layoutToShow.setVisibility(View.VISIBLE);
                        layoutToShow.animate().alpha(1.0f).setDuration(300);
                    }
                });
    }

}