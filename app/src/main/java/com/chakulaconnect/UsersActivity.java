package com.chakulaconnect;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;

public class UsersActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener {

    private DatabaseReference databaseReference;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private ArrayList<UserModel> userModels;
    UsersAdapter usersAdapter;
    private RecyclerView userRecycleView;
    FlexboxLayout fbFilter;
    TextView txt_btnFilter;
    Boolean filterIndividual, filterOrganisation, isDonor = false, isRecipient = false, filterDonor, filterRecipient;
    SharedPreferences sharedPreferences;
    Gson gson;
    String userData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_users);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        gson = new Gson();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userRecycleView = findViewById(R.id.rv_users);
        fbFilter = findViewById(R.id.fbFilters);
        txt_btnFilter = findViewById(R.id.txt_btnFilters);
        filterIndividual = false;
        filterOrganisation = false;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        userRecycleView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        userModels = new ArrayList<>();

        if(isUser()){
            sharedPreferences = getSharedPreferences(user.getUid()+"_pref", MODE_PRIVATE);
            userData = sharedPreferences.getString(user.getUid()+"_data", null);
            if (userData != null){
                UserModel userModel = gson.fromJson(userData, UserModel.class);
                if (userModel.getAccount_role().containsKey("Donor")){
                    isDonor = true;
                } else if (userModel.getAccount_role().containsKey("Recipient")) {
                    isRecipient = true;
                }
            }

            filterDonor = isRecipient;
            filterRecipient = isDonor;
            if(filterDonor){
                addFilterTextView("Donor", fbFilter);
            }else{
                if(filterRecipient){
                    addFilterTextView("Recipient", fbFilter);
                }
            }
            loadUsers(filterDonor, filterRecipient, filterOrganisation, filterIndividual);
        }
        txt_btnFilter.setOnClickListener(v->{
            createFilterDialog().show();
        });
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if(firebaseAuth.getCurrentUser() == null){
            startActivity(new Intent(this, AuthLogin.class));
            finish();
        }
    }
    private boolean isUser(){
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_search_menu, menu);
        return super.onCreateOptionsMenu(menu);
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
    private AlertDialog createFilterDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.filter_layout, null);
        RadioGroup rgRoles = view.findViewById(R.id.rgRoleFilter);
        RadioGroup rgEntity = view.findViewById(R.id.rgTypeFilter);
        builder.setTitle("Select filter");
        if(filterRecipient){
            RadioButton radioButton = view.findViewById(R.id.rbRecipientFilter);
            radioButton.setChecked(true);
        }else{
            if(filterDonor){
                RadioButton radioButton = view.findViewById(R.id.rbDonorFilter);
                radioButton.setChecked(true);
            }
        }
        if(filterIndividual){
            RadioButton radioButton = view.findViewById(R.id.rbIndividualFilter);
            radioButton.setChecked(true);
        }else {
            if(filterOrganisation){
                RadioButton radioButton = view.findViewById(R.id.rbOrganisationFilter);
                radioButton.setChecked(true);
            }
        }
        if(!filterIndividual && !filterOrganisation){
            RadioButton radioButton = view.findViewById(R.id.rbTypeAllFilter);
            radioButton.setChecked(true);
        }
        if(!filterRecipient && !filterDonor){
            RadioButton radioButton = view.findViewById(R.id.rbRoleAllFilter);
            radioButton.setChecked(true);
        }
        builder.setPositiveButton("Apply", (dialog, which)->{
            //Apply filters
            if(rgRoles.getCheckedRadioButtonId() != -1 || rgEntity.getCheckedRadioButtonId() != -1){
                fbFilter.removeAllViews();
            }
            if (rgRoles.getCheckedRadioButtonId() != -1) {
                RadioButton selectedRole = view.findViewById(rgRoles.getCheckedRadioButtonId());
                if (selectedRole.getId() == R.id.rbRecipientFilter) {
                    addFilterTextView("Recipient", fbFilter);
                    filterRecipient = true;
                    filterDonor = false;
                } else if (selectedRole.getId() == R.id.rbDonorFilter) {
                    filterDonor = true;
                    filterRecipient = false;
                    addFilterTextView("Donor", fbFilter);
                } else {
                    // Do something
                    addFilterTextView("All", fbFilter);
                    filterDonor = false;
                    filterRecipient = false;
                }
            }

            if (rgEntity.getCheckedRadioButtonId() != -1) {
                RadioButton selectedEntity = view.findViewById(rgEntity.getCheckedRadioButtonId());
                if (selectedEntity.getId() == R.id.rbIndividualFilter) {
                    addFilterTextView("Individual", fbFilter);
                    filterOrganisation = false;
                    filterIndividual = true;
                } else if (selectedEntity.getId() == R.id.rbOrganisationFilter) {
                    addFilterTextView("Organisation", fbFilter);
                    filterOrganisation = true;
                    filterIndividual = false;
                } else {
                    // Do something
                    addFilterTextView("All", fbFilter);
                    filterOrganisation = false;
                    filterIndividual = false;
                }
            }
            if(rgRoles.getCheckedRadioButtonId() != -1 || rgEntity.getCheckedRadioButtonId() != -1){
                loadUsers(filterDonor, filterRecipient, filterOrganisation, filterIndividual);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which)->{
            dialog.dismiss();
        });
        builder.setView(view);
        return builder.create();
    }
    private void addFilterTextView(String text, ViewGroup parent) {
        ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, // Width
                ViewGroup.LayoutParams.WRAP_CONTENT  // Height
        );
        layoutParams.setMargins(8, 2, 0, 2);
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(5, 5, 5, 5);
        textView.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.filters_bg, getTheme()));
        textView.setLayoutParams(layoutParams);
        parent.addView(textView);
    }
    private void loadUsers(Boolean F_DON, Boolean F_REC, Boolean F_ORG, Boolean F_IND){
        userModels.clear();
        AlertDialog alertDialog = Progress.createAlertDialog(this, "Please wait");
        alertDialog.show();
        databaseReference.child("Users").get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        alertDialog.dismiss();
                        DataSnapshot snapshot = task.getResult();
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            if(dataSnapshot.exists()){
                                if(!dataSnapshot.getKey().equals(user.getUid())){
                                    if(F_DON){
                                        if(dataSnapshot.child("account_role").hasChild("Donor")){
                                            if(filterEntity(dataSnapshot, F_ORG, F_IND) != null){
                                                userModels.add(filterEntity(dataSnapshot, F_ORG, F_IND));
                                            }
                                        }
                                    }else if(F_REC){
                                        if(dataSnapshot.child("account_role").hasChild("Recipient")){
                                            if(filterEntity(dataSnapshot, F_ORG, F_IND) != null){
                                                userModels.add(filterEntity(dataSnapshot, F_ORG, F_IND));
                                            }
                                        }
                                    }else{
                                        if(filterEntity(dataSnapshot, F_ORG, F_IND) != null){
                                            userModels.add(filterEntity(dataSnapshot, F_ORG, F_IND));
                                        }
                                    }
                                }

                            }

                        }
                        usersAdapter = new UsersAdapter(userModels, getApplicationContext());
                        userRecycleView.setAdapter(usersAdapter);
                        usersAdapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(e->{
                    alertDialog.dismiss();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private UserModel filterEntity(DataSnapshot dataSnapshot, Boolean filterOrganisation, Boolean filterIndividual){
        Log.d("FilterEntity", "Filter Org: " + filterOrganisation + " Filter Ind: " + filterIndividual);
        if(filterOrganisation){
            if(dataSnapshot.child("account_role").hasChild("Organisation")){
                return dataSnapshot.getValue(UserModel.class);
            }else{
                return null;
            }
        }else if (filterIndividual) {
            if(dataSnapshot.child("account_role").hasChild("Individual")){
                return dataSnapshot.getValue(UserModel.class);
            }else {
                return null;
            }
        }
        return dataSnapshot.getValue(UserModel.class);
    }


}