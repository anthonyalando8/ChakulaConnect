package com.chakulaconnect;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class PersonalInfo extends AppCompatActivity implements FirebaseAuth.AuthStateListener {
    FirebaseUser user;
    FirebaseAuth auth;
    String userId;
    DatabaseReference databaseReference;
    EditText etPhone, etCountry, etCounty, etAddress, etMoreInfo, etCompanyName, etCity, etOtherBusiness;
    LinearLayout llOrgInfo;
    SharedPreferences sharedPreferences;
    Gson gson;
    Spinner spBusType;
    MaterialButton btnSave;

    TextView txtInfoError;
    LocationModel locationModel;
    String country = "", county = "", city = "", streetAddress = "", strLongitude = "", strLatitude = "";
    String userData;
    UserModel userModel;
    Boolean isDonor = false, isRecipient=false, isOrg= false, isIndividual= false, isOtherBus = false;
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
        gson = new Gson();

        etPhone = findViewById(R.id.etPhone);
        etCountry = findViewById(R.id.etCountry);
        etCounty = findViewById(R.id.etCounty);
        etAddress = findViewById(R.id.etAddress);
        etMoreInfo = findViewById(R.id.etMoreDesc);
        etCity = findViewById(R.id.etCity);
        etCompanyName = findViewById(R.id.etCompanyName);
        etOtherBusiness = findViewById(R.id.etOrganisationTypeOther);

        spBusType = findViewById(R.id.spCompanyType);

        btnSave = findViewById(R.id.btnSave);
        txtInfoError = findViewById(R.id.txtInfoError);

        llOrgInfo = findViewById(R.id.llOrganisationInfo);

        setSpinnerAdapter(R.array.business_type, spBusType);
        if(isUser()){
            userId = user.getUid();
            sharedPreferences = getSharedPreferences(userId.concat("_pref"), MODE_PRIVATE);
            userData = sharedPreferences.getString(userId.concat("_data"), null);

            if(userData != null){
                 userModel = gson.fromJson(userData, UserModel.class);
                if(userModel.getAccount_role().containsKey("Organisation")){
                    isOrg = true;
                } else if (userModel.getAccount_role().containsKey("Individual")) {
                    isIndividual = true;
                }
                if(userModel.getAccount_role().containsKey("Donor")){
                    isDonor = true;
                }else if(userModel.getAccount_role().containsKey("Recipient")){
                    isRecipient = true;
                }
            }

            llOrgInfo.setVisibility((isOrg && isDonor) ? View.VISIBLE : View.GONE);

            LocationUtil locationUtil = new LocationUtil(this);
            locationUtil.requestLocationUpdates(new LocationUtil.LocationCallback() {
                @Override
                public void onLocationResult(Location location, String country, String region, String city, String streetAddress, String countryCode, String formattedAddress) {
                    locationModel = new LocationModel(country, region, city, streetAddress, Double.toString(location.getLongitude()), Double.toString(location.getLatitude()));
                    setLocationDetails(Double.toString(location.getLongitude()), Double.toString(location.getLatitude()), country, region, city, streetAddress);
                }
            });
        }
        String[] bussType = getResources().getStringArray(R.array.business_type);
        final String[] BusinessType = {""};
        spBusType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                BusinessType[0] = adapterView.getItemAtPosition(i).toString();
                if(BusinessType[0].equals(bussType[bussType.length - 1])){
                    etOtherBusiness.setVisibility(View.VISIBLE);
                    isOtherBus = true;
                }else {
                    isOtherBus = false;
                    etOtherBusiness.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        if(isOtherBus){
            BusinessType[0] = etOtherBusiness.getText().toString().trim();
        }
        btnSave.setOnClickListener(v->{
            Map<String, Object> moreInfo = new HashMap<>();

            String Phone = etPhone.getText().toString().trim();
            String Country = etCountry.getText().toString().trim();
            String County = etCounty.getText().toString().trim();
            String City = etCity.getText().toString().trim();
            String CompanyName = etCompanyName.getText().toString().trim();
            String other_bus = etOtherBusiness.getText().toString().trim();
            String bus_type = spBusType.getSelectedItem().toString();

            String Address = etAddress.getText().toString().trim();
            String MoreInfo = etMoreInfo.getText().toString().trim();
            if(validate(Phone, Country, County, City, Address, MoreInfo, CompanyName,bus_type, other_bus, isDonor, isOrg )){
                locationModel = new LocationModel(Country, County, City, Address, strLongitude, strLatitude);
                moreInfo.put("phone", Phone);
                moreInfo.put("moreInfo", MoreInfo);
                moreInfo.put("location", locationModel);
                if (isDonor && isOrg){
                    moreInfo.put("companyName", CompanyName);
                    moreInfo.put("companyBusiness",BusinessType[0]);
                }

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

    private boolean validate(String PHONE, String COUNTRY,String COUNTY,String CITY, String ADDRESS,String MORE, String COMPANY_NAME, String BUSS_TYPE, String OTHER_BUSS, Boolean isOrg, Boolean isDonor){
        if( MORE.length() > 150){
            etMoreInfo.setError("Exceeds 150 characters");
            return false;
        }
        if(PHONE.isEmpty() | COUNTRY.isEmpty() | COUNTY.isEmpty() | CITY.isEmpty() | ADDRESS.isEmpty()){
            txtInfoError.setText("All non-optional fields required");
            txtInfoError.setVisibility(View.VISIBLE);
            return false;
        }
        if (isDonor && isOrg){
            if(COMPANY_NAME.isEmpty()){
                etCompanyName.setError("Required");
                return false;
            }
            if (BUSS_TYPE.equals("Other") && OTHER_BUSS.isEmpty()){
                etOtherBusiness.setError("Required");
                return false;
            }
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
    private void setLocationDetails(String strLong, String strLat, String strCountry, String strCounty, String strCity, String strStreetAddress){
        strLatitude = strLat;
        strLongitude = strLong;
        country = strCountry;
        county = strCounty;
        city = strCity;

        streetAddress = strStreetAddress;
        etCountry.setText(strCountry);
        etCounty.setText(strCounty);
        etCity.setText(strCity);
        etAddress.setText(strStreetAddress);
    }
    private void setSpinnerAdapter(int arrayValues, Spinner spinner){
        ArrayAdapter<CharSequence> food_type_donor_adapter = ArrayAdapter.createFromResource(
                this, arrayValues, android.R.layout.simple_spinner_item
        );
        food_type_donor_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(food_type_donor_adapter);
    }

}