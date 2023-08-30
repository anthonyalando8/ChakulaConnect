package com.chakulaconnect;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class PersonalInfo extends AppCompatActivity implements FirebaseAuth.AuthStateListener {
    FirebaseUser user;
    FirebaseAuth auth;
    String userId;
    DatabaseReference databaseReference;
    EditText etPhone, etCountry, etCounty, etAddress, etMoreInfo;
    MaterialButton btnSave;

    TextView txtInfoError;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null){
            getSupportActionBar().setSubtitle("Personalize your profile");
        }
        setContentView(R.layout.activity_personal_info);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        etPhone = findViewById(R.id.etPhone);
        etCountry = findViewById(R.id.etCountry);
        etCounty = findViewById(R.id.etCounty);
        etAddress = findViewById(R.id.etAddress);
        etMoreInfo = findViewById(R.id.etMoreDesc);
        btnSave = findViewById(R.id.btnSave);
        txtInfoError = findViewById(R.id.txtInfoError);

        if(isUser()){
            userId = user.getUid();
        }
        btnSave.setOnClickListener(v->{
            Map<String, Object> moreInfo = new HashMap<>();

            String Phone = etPhone.getText().toString().trim();
            String Country = etCountry.getText().toString().trim();
            String County = etCounty.getText().toString().trim();
            String Address = etAddress.getText().toString().trim();
            String MoreInfo = etMoreInfo.getText().toString().trim();

            if(validate(Phone, Country, County, Address, MoreInfo)){
                moreInfo.put("phone", Phone);
                moreInfo.put("country", Country);
                moreInfo.put("county", County);
                moreInfo.put("address", Address);
                moreInfo.put("moreInfo", MoreInfo);

                updateUserInfo(moreInfo);
            }

        });

    }

    private boolean isUser(){
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            return true;
        }
        return false;
    }

    private void updateUserInfo(Map<String, Object> moreInfo){
        AlertDialog alertDialog = Progress.createAlertDialog(this, "Updating...");
        alertDialog.show();
        AtomicBoolean updated = new AtomicBoolean(false);
        databaseReference.child("Users").child(userId).child("moreInfo").updateChildren(moreInfo, (error, ref) -> {
            if(error != null){
                alertDialog.dismiss();
                Toast.makeText(PersonalInfo.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }else{

                TextView alertMessage = alertDialog.findViewById(R.id.txtLoading);
                if(alertMessage != null){
                    alertMessage.setText("Finalizing...");
                }
                databaseReference.child("Users").child(userId).child("account_role").child("complete")
                                .setValue(true).addOnCompleteListener(task -> {
                                    if(task.isSuccessful()){

                                        alertDialog.dismiss();
                                        updated.set(true);
                                        handlesUpdates(updated.get());
                                    }
                                });
            }
        });
    }

    private boolean validate(String PHONE, String COUNTRY,String COUNTY,String ADDRESS,String MORE){
        if( MORE.length() > 150){
            etMoreInfo.setError("Exceeds 150 characters");
            return false;
        }
        if(PHONE.isEmpty() | COUNTRY.isEmpty() | COUNTY.isEmpty() | ADDRESS.isEmpty()){
            txtInfoError.setText("All non-optional fields required");
            txtInfoError.setVisibility(View.VISIBLE);
            return false;
        }
        txtInfoError.setVisibility(View.GONE);
        return true;
    }

    private void handlesUpdates(Boolean RESULTS){
        if(RESULTS){
            Intent mainIntent = new Intent(this, MainActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
        }
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        user = firebaseAuth.getCurrentUser();
        if(user == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Login required");
            builder.setMessage("Login to your account to continue");

            builder.setCancelable(false);
            builder.setPositiveButton("Ok", (dialog, which)->{
                Intent loginIntent = new Intent(this, AuthLogin.class);
                loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(loginIntent);
            });
        }
    }

}