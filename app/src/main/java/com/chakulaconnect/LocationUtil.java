package com.chakulaconnect;

import static androidx.core.content.ContextCompat.startActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.List;
import java.util.Locale;

public class LocationUtil {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private Context context;
    PermissionUtil permissionUtil;
    public interface LocationCallback {
        void onLocationResult(Location location, String country, String region, String city, String streetAddress, String countryCode, String formattedAddress);
    }
    public LocationUtil(Context context){
        this.context = context;
        permissionUtil = new PermissionUtil(context);
    }

    public void requestLocationUpdates(LocationCallback locationCallback){
        permissionUtil.requestLocationPermission(new PermissionUtil.locationPermissionCallback() {
            @Override
            public void onPermission(Boolean RESULT) {
                if(RESULT){
                    getLocation(locationCallback);
                }else{
                    ActivityCompat.requestPermissions((Activity) context,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_PERMISSION_REQUEST_CODE);
                    getLocation(locationCallback);
                }
            }
        });
    }

    public void getLocation(LocationCallback locationCallback){
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if(locationManager != null && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new android.location.LocationListener(){

                @Override
                public void onLocationChanged(@NonNull Location location) {
                    String country, region, city, address, countryCode, fullFormattedAddress;
                    Address address1 = getAddressFromLocation(location);
                    if(address1 != null){
                        country = address1.getCountryName();
                        region = address1.getAdminArea();
                        city = address1.getLocality();
                        address = address1.getAddressLine(0);
                        countryCode = address1.getCountryCode();

                        StringBuilder formattedAddress = new StringBuilder();
                        for (int i = 0; i <= address1.getMaxAddressLineIndex(); i++) {
                            formattedAddress.append(address1.getAddressLine(i));
                            if (i < address1.getMaxAddressLineIndex()) {
                                formattedAddress.append(", "); // Add comma between lines
                            }
                        }
                        fullFormattedAddress = formattedAddress.toString();
                    }else {
                        country = region = city  = address = countryCode = fullFormattedAddress = "Unknown";
                    }
                    locationCallback.onLocationResult(location, country, region, city, address, countryCode, fullFormattedAddress);
                    locationManager.removeUpdates(this);
                }

                @Override
                public void onProviderDisabled(@NonNull String provider) {
                    //LocationListener.super.onProviderDisabled(provider);
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle("Location request");
                    alertDialog.setMessage("To ensure optimal app functionality, granting location access is essential. Please open settings to enable location services.");
                    alertDialog.setCancelable(false);
                    alertDialog.setPositiveButton("Ok", (dialog, which)->{
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        context.startActivity(intent);
                    });
                    alertDialog.create().show();
                }
            });

        }
    }
    private Address getAddressFromLocation(Location location){
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            if(!addresses.isEmpty()){
                return addresses.get(0);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
