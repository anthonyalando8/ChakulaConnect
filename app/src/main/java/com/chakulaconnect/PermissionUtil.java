package com.chakulaconnect;

import android.Manifest;
import android.content.Context;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class PermissionUtil {
    Context context;

    public PermissionUtil(Context context) {
        this.context = context;
    }
    public interface cameraPermissionCallback{
        void onPermission(Boolean RESULT);
    }
    public interface storagePermissionCallback{
        void onPermission(Boolean RESULT);
    }
    public interface locationPermissionCallback{
        void onPermission(Boolean RESULT);
    }
    public void requestCameraPermission( cameraPermissionCallback permissionCallback){
        Dexter.withContext(context)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        permissionCallback.onPermission(true);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        permissionCallback.onPermission(false);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionCallback.onPermission(false);
                        requestCameraPermission(permissionCallback);
                    }
                }).check();
    }
    public void requestStoragePermission(storagePermissionCallback permissionCallback){
        Dexter.withContext(context)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        permissionCallback.onPermission(true);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        permissionCallback.onPermission(false);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        requestStoragePermission(permissionCallback);
                    }
                }).check();
    }
    public void requestLocationPermission(locationPermissionCallback permissionCallback){
        Dexter.withContext(context)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        permissionCallback.onPermission(true);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        permissionCallback.onPermission(false);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        requestLocationPermission(permissionCallback);
                    }
                });
    }
}
