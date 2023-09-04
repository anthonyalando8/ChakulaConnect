package com.chakulaconnect;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

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

public class ActivityDonationInfo extends AppCompatActivity implements FirebaseAuth.AuthStateListener {

    Spinner spFoodTypeDonor, spQuantityDonor, spPackaging, spFoodTypeRecipient, spFoodQuantityRecipient, spFoodUrgency, spQuality;
    EditText etFoodQuantityDonor, etStorageConditions, etHandlingInst, etSpecialConsiderations, etFoodQuantityRecipient, etPackDate, etExpiryDate;
    MaterialButton btnCancel, btnSubmit;
    RadioGroup rgAddress;
    EditText etAddress, etPickDeliveryCounty, etPickDeliveryTown;
    ConstraintLayout clMakeRequest,clDonate;
    TextView txtDonRecHead, txtAddressLbl;
    LinearLayout llExpandCollapse, llDonorOptional, llPackDate, llPickDeliveryLocation;
    Boolean isDonor = false, isRecipient = false;
    Gson gson;
    SharedPreferences sharedPreferences;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    String userId, userData, addressLoc = "Unknown", formatAddress = "Unknown", countyLoc = "unknown", cityLoc = "unknown";
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
        txtAddressLbl = findViewById(R.id.txtAddressLbl);

        etFoodQuantityDonor = findViewById(R.id.etQuantity);
        etPackDate = findViewById(R.id.etPreparationDate);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        etStorageConditions = findViewById(R.id.etStorageConditions);
        etHandlingInst = findViewById(R.id.etHandling);
        etSpecialConsiderations = findViewById(R.id.etSpecialConsiderations);
        etFoodQuantityRecipient = findViewById(R.id.etQuantityRecipient);
        etAddress = findViewById(R.id.etPickDeliveryAddress);
        etPickDeliveryTown = findViewById(R.id.etPickDeliveryTown);
        etPickDeliveryCounty = findViewById(R.id.etPickDeliveryCounty);

        clDonate = findViewById(R.id.clDonateFood);
        clMakeRequest = findViewById(R.id.clMakeRequest);

        llDonorOptional = findViewById(R.id.llDonorOptional);
        llExpandCollapse = findViewById(R.id.llExpandCollapse);
        llPackDate = findViewById(R.id.llPackDate);
        llPickDeliveryLocation = findViewById(R.id.llPickDeliveryLocation);

        rgAddress = findViewById(R.id.rgPickUpPoint);

        if(isUser()){
            userId = user.getUid();
            sharedPreferences = getSharedPreferences(userId+"_pref", MODE_PRIVATE);
            userData = sharedPreferences.getString(userId+"_data", null);
            LocationUtil locationUtil = new LocationUtil(this);
            locationUtil.requestLocationUpdates(new LocationUtil.LocationCallback() {
                @Override
                public void onLocationResult(Location location, String country, String region, String city, String streetAddress, String countryCode, String formattedAddress) {
                    formatAddress = formattedAddress;
                    countyLoc = region;
                    cityLoc = city;
                }
            });

            if(userData != null){
                UserModel userModel = gson.fromJson(userData, UserModel.class);
                addressLoc = userModel.getMoreInfo().get("address").toString();
                if(userModel.getAccount_role().containsKey("Donor")){
                    isDonor = true;
                    clDonate.setVisibility(View.VISIBLE);
                    txtAddressLbl.setText("Pick up address");
                    txtDonRecHead.setText("Kindly furnish us with the specifics of your food donation.");
                    getSupportActionBar().setTitle("Donate food");
                } else if (userModel.getAccount_role().containsKey("Recipient")) {
                    isRecipient = true;
                    txtAddressLbl.setText("Delivery address");
                    clMakeRequest.setVisibility(View.VISIBLE);
                    getSupportActionBar().setTitle("Make request");
                    txtDonRecHead.setText("Please complete the fields below to submit your request.");
                }
            }

        }
        etExpiryDate.setOnClickListener(v-> setDate(true, false, etExpiryDate));
        etPackDate.setOnClickListener(v-> setDate(false, true, etPackDate));
        setSpinnerAdapter(R.array.food_type_donor_array, spFoodTypeDonor);
        setSpinnerAdapter(R.array.quantity, spQuantityDonor);
        setSpinnerAdapter(R.array.food_quality, spQuality);
        setSpinnerAdapter(R.array.food_packaging, spPackaging);
        setSpinnerAdapter(R.array.quantity, spFoodQuantityRecipient);
        setSpinnerAdapter(R.array.food_urgency, spFoodUrgency);
        setSpinnerAdapter(R.array.food_type_recipient, spFoodTypeRecipient);

        llExpandCollapse.setOnClickListener(V->{
            if(llDonorOptional.getVisibility() == View.GONE){
                expand(llDonorOptional);
            }else {
                collapse(llDonorOptional);
            }
        });
        KeyListener etAddressKey = etAddress.getKeyListener();
        rgAddress.setOnCheckedChangeListener((radioGroup, i) -> {
            expand(llPickDeliveryLocation);
            //llPickDeliveryLocation.setVisibility(View.VISIBLE);
            switch (i){
                case R.id.rbDefaultAddress:{
                    etAddress.setText(addressLoc);
                    disableViews(null);
                    break;
                }
                case R.id.rbSpecifyAddress:{
                    etAddress.setText(formatAddress);
                    etPickDeliveryCounty.setText(countyLoc);
                    etPickDeliveryTown.setText(cityLoc);

                    disableViews(etAddressKey);
                    break;
                }
            }
        });
        String[] foodTypes = getResources().getStringArray(R.array.food_type_donor_array);
        spFoodTypeDonor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapterView.getItemAtPosition(i).toString().equals(foodTypes[1])){
                    llPackDate.setVisibility(View.VISIBLE);
                }else {
                    llPackDate.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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
    private void disableViews(KeyListener listener){
        for (int i = 0; i < llPickDeliveryLocation.getChildCount(); i++) {
            if (llPickDeliveryLocation.getChildAt(i) instanceof RelativeLayout) {
                RelativeLayout relativeLayout = (RelativeLayout) llPickDeliveryLocation.getChildAt(i);

                for (int j = 0; j < relativeLayout.getChildCount(); j++) {
                    if (relativeLayout.getChildAt(j) instanceof LinearLayout) {
                        LinearLayout linearLayout = (LinearLayout) relativeLayout.getChildAt(j);

                        for (int k = 0; k < linearLayout.getChildCount(); k++) {
                            if (linearLayout.getChildAt(k) instanceof EditText) {
                                EditText editText = (EditText) linearLayout.getChildAt(k);
                                editText.setKeyListener(listener);
                            }
                        }
                    }
                }
            }
        }

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
        Calendar minDateExpiry = Calendar.getInstance();
        minDateExpiry.add(Calendar.DAY_OF_MONTH, 5);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                android.R.style.Theme_Holo_Light_Dialog,
                (datePicker, i, i1, i2) -> {
                    currentDate.set(i, i1, i2);
                    // Format the selected date
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    selectedDate[0] = dateFormat.format(currentDate.getTime());
                    editText.setText(selectedDate[0]);
                }, currentYear, currentMonth, currentDay);
        if(isExpiry){
            // Set the minimum and maximum dates for the DatePickerDialog
            datePickerDialog.getDatePicker().setMinDate(minDateExpiry.getTimeInMillis());
            datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        } else if (isPack) {
            // Set the minimum and maximum dates for the DatePickerDialog
            datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
            datePickerDialog.getDatePicker().setMaxDate(currentDate.getTimeInMillis());
        }
        datePickerDialog.show();

    }
    private void setSpinnerAdapter(int arrayValues, Spinner spinner){
        ArrayAdapter<CharSequence> food_type_donor_adapter = ArrayAdapter.createFromResource(
                this, arrayValues, android.R.layout.simple_spinner_item
        );
        food_type_donor_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(food_type_donor_adapter);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if(firebaseAuth.getCurrentUser() == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.info, getTheme()));
            builder.setTitle("Alert");
            builder.setMessage("Login required");
            builder.setCancelable(false);
            builder.setPositiveButton("Login", (dialog, which)->{
                Intent authIntent = new Intent(this, AuthLogin.class);
                authIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                dialog.dismiss();
                startActivity(authIntent);
            });
            builder.create().show();
        }
    }
    private void expand(final View view) {
        view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = view.getMeasuredHeight();

        view.getLayoutParams().height = 1;
        view.setVisibility(View.VISIBLE);

        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                view.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                view.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        animation.setDuration((int) (targetHeight / view.getContext().getResources().getDisplayMetrics().density));
        view.startAnimation(animation);
    }

    private void collapse(final View view) {
        final int initialHeight = view.getMeasuredHeight();

        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    view.setVisibility(View.GONE);
                } else {
                    view.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    view.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        animation.setDuration((int) (initialHeight / view.getContext().getResources().getDisplayMetrics().density));
        view.startAnimation(animation);
    }
}