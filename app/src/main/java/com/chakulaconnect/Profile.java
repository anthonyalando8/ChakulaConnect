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
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity implements FirebaseAuth.AuthStateListener {
    CircleImageView civUserImage;
    TextView txtUserName, txtUserEmail, btnAccountDetails, btnPersonalInfo, txtRole, txtType, txtComplete, txtJoinDate;
    TextView txtPhone, txtAddress, txtCountry, txtRegion, txtMoreInfo;
    ConstraintLayout clAccDetails, clPersonInfo, clIsUser;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference databaseReference;
    String userName, userEmail, imageUri, userId, role, entityType;
    Long joinDate;
    SharedPreferences sharedPreferences;
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

        userId = getIntent().getStringExtra("userId");
        userName = getIntent().getStringExtra("displayName");
        userEmail = getIntent().getStringExtra("email");
        if (isUser()){
            if(userId != null){
                if(userId.equals(user.getUid())) {
                    if(getSupportActionBar() !=  null){
                        getSupportActionBar().setTitle("My profile");
                    }
                    clIsUser.setVisibility(View.VISIBLE);
                    userName = user.getDisplayName();
                    userEmail = user.getEmail();
                    imageUri = user.getPhotoUrl().toString();
                    sharedPreferences = getSharedPreferences(userId + "_pref", MODE_PRIVATE);

                }else{
                    if(getSupportActionBar() !=  null){
                        getSupportActionBar().setTitle(userName);
                        getSupportActionBar().setSubtitle(userEmail);
                    }
                    clIsUser.setVisibility(View.GONE);
                }
                retrievePersonalInfo(userId);
            }


        }
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
                .get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if(task.isSuccessful()){
                            alertDialog.dismiss();
                            DataSnapshot snapshot = task.getResult();

                            UserModel userModel = snapshot.getValue(UserModel.class);
                            txtPhone.setText(userModel.getMoreInfo().get("phone").toString());
                            txtAddress.setText(userModel.getMoreInfo().get("address").toString());
                            txtCountry.setText(userModel.getMoreInfo().get("country").toString());
                            txtRegion.setText(userModel.getMoreInfo().get("county").toString());
                            txtMoreInfo.setText(userModel.getMoreInfo().get("moreInfo").toString());

                            if(snapshot.child("account_role").hasChild("Donor")){
                                role = "Donor";
                            }else if(snapshot.child("account_role").hasChild("Recipient")){
                                role = "Recipient";
                            }
                            if(snapshot.child("account_role").hasChild("Organisation")){
                                entityType = "Organisation";
                            }else if(snapshot.child("account_role").hasChild("Individual")){
                                entityType = "Individual";
                            }
                            txtUserEmail.setText(userModel.getAccountDetails().get("email"));
                            txtUserName.setText(userModel.getAccountDetails().get("userName"));
                            if(userModel.getAccountDetails().containsKey("coverUri") && !userModel.getAccountDetails().get("coverUri").isEmpty()){
                                Picasso.get()
                                        .load(userModel.getAccountDetails().get("coverUri"))
                                        .into(iv_user_cover);
                            }
                            Picasso.get().load(userModel.getAccountDetails().get("imageUri")).into(civUserImage);

                            txtType.setText(entityType);
                            txtRole.setText(role);

                            txtComplete.setText(userModel.getAccount_role().get("complete").toString());

                            txtJoinDate.setText(TimeCalculator.calculateTime(Long.parseLong(userModel.getAccountDetails().get("joinDate"))));
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