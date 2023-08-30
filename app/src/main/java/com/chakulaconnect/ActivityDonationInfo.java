package com.chakulaconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class ActivityDonationInfo extends AppCompatActivity {

    Spinner spFoodTypeDonor, spQuantityDonor, spPackaging, spFoodTypeRecipient, spFoodQuantityRecipient, spFoodUrgency, spQuality;
    EditText etFoodQuantityDonor, etStorageConditions, etHandlingInst, etSpecialConsiderations, etFoodQuantityRecipient, etPackDate, etExpiryDate;
    MaterialButton btnCancel, btnSubmit;
    ConstraintLayout clMakeRequest,clDonate;
    TextView txtDonRecHead;
    Boolean isDonor = false, isRecipient = false;
    Gson gson;
    SharedPreferences sharedPreferences;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    String userId, userData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_info);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        gson = new Gson();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        spFoodTypeDonor = findViewById(R.id.spFoodType);
        spFoodTypeRecipient = findViewById(R.id.spFoodTypeRecipient);
        spPackaging = findViewById(R.id.spPackage);
        spFoodUrgency = findViewById(R.id.spFoodUrgency);
        spQuantityDonor = findViewById(R.id.spQuantityUnits);
        spQuality = findViewById(R.id.spQuality);
        spFoodQuantityRecipient = findViewById(R.id.spQuantityUnitsRecipient);

        txtDonRecHead = findViewById(R.id.lblInfoTop);

        etFoodQuantityDonor = findViewById(R.id.etQuantity);
        etPackDate = findViewById(R.id.etPreparationDate);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        etStorageConditions = findViewById(R.id.etStorageConditions);
        etHandlingInst = findViewById(R.id.etHandling);
        etSpecialConsiderations = findViewById(R.id.etSpecialConsiderations);
        etFoodQuantityRecipient = findViewById(R.id.etQuantityRecipient);

        clDonate = findViewById(R.id.clDonateFood);
        clMakeRequest = findViewById(R.id.clMakeRequest);

        if(isUser()){
            userId = user.getUid();
            sharedPreferences = getSharedPreferences(userId+"_pref", MODE_PRIVATE);
            userData = sharedPreferences.getString(userId+"_data", null);

            if(userData != null){
                UserModel userModel = gson.fromJson(userData, UserModel.class);
                if(userModel.getAccount_role().containsKey("Donor")){
                    isDonor = true;
                    clDonate.setVisibility(View.VISIBLE);
                    txtDonRecHead.setText("Kindly furnish us with the specifics of your food donation.");
                } else if (userModel.getAccount_role().containsKey("Recipient")) {
                    isRecipient = true;
                    clMakeRequest.setVisibility(View.VISIBLE);
                    txtDonRecHead.setText("Please complete the fields below to submit your request.");
                }
            }

        }
        etExpiryDate.setOnClickListener(v->{
            setDate(true, false, etExpiryDate);
        });
        etPackDate.setOnClickListener(v->{
            setDate(false, true, etPackDate);
        });
//        ArrayAdapter<CharSequence> food_type_donor_adapter = ArrayAdapter.createFromResource(
//                this, R.array.food_type_donor_array, android.R.layout.simple_spinner_item
//        );
//
//        spFoodTypeDonor.setAdapter(food_type_donor_adapter);
        // Retrieve the list of items from strings.xml
        String[] spinnerItems = getResources().getStringArray(R.array.food_type_donor_array);

        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(this, Arrays.asList(spinnerItems));
        spFoodTypeDonor.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{
                onBackPressed();
            }
        }
        return super.onOptionsItemSelected(item);
    }
    private boolean isUser(){
        if(user != null){
            return true;
        }
        return false;
    }
    private void setDate(boolean isExpiry, boolean isPack, EditText editText){
        final String[] selectedDate = {""};
        Calendar currentDate = Calendar.getInstance();
        int currentYear = currentDate.get(Calendar.YEAR);
        int currentMonth = currentDate.get(Calendar.MONTH);
        int currentDay = currentDate.get(Calendar.DAY_OF_MONTH);
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, 5);
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -5);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                android.R.style.Theme_Holo_Light_Dialog,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        currentDate.set(i, i1, 12);
                        // Format the selected date
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                        selectedDate[0] = dateFormat.format(currentDate.getTime());
                        editText.setText(selectedDate[0]);
                    }
                }, currentYear, currentMonth, currentDay);
        if(isExpiry){
            // Set the minimum and maximum dates for the DatePickerDialog
            datePickerDialog.getDatePicker().setMinDate(currentDate.getTimeInMillis());
            datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        } else if (isPack) {
            // Set the minimum and maximum dates for the DatePickerDialog
            datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
            datePickerDialog.getDatePicker().setMaxDate(currentDate.getTimeInMillis());
        }
        datePickerDialog.show();

    }
}