package com.chakulaconnect;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Gallery;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class ImageUtil {
    Context context;
    Activity activity;
    PermissionUtil permissionUtil;
    AlertDialog.Builder alertdialog;
    static int REQUEST_GALLERY_PICK = 200;
    static int REQUEST_CAPTURE = 201;
    ImageCompressor imageCompressor;
    int targetWidth, targetHeight, quality;

    public ImageUtil(Context context, Activity activity, int quality) {
        this.context = context;
        this.activity = activity;
        permissionUtil = new PermissionUtil(context);
        alertdialog = new AlertDialog.Builder(context);
        this.quality = quality;

    }
    public void createImageDialog(){
        alertdialog.setTitle("Select source");
        String [] options = {"Gallery", "Camera"};
        alertdialog.setItems(options, (dialog, which)->{
            if(which == 0){
                openGallery(activity);
            } else if (which == 1) {
                openCamera(activity);
            }
        });
        alertdialog.setNegativeButton("Cancel", (dialog, which)->{
            dialog.dismiss();
        });
        alertdialog.create().show();
    }
    public void openCamera(Activity activity){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        permissionUtil.requestCameraPermission(new PermissionUtil.cameraPermissionCallback() {
            @Override
            public void onPermission(Boolean RESULT) {
                if(RESULT){
                    if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
                        activity.startActivityForResult(takePictureIntent, REQUEST_CAPTURE);
                    }
                }
            }
        });

    }
    public void openGallery(Activity activity){
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        permissionUtil.requestStoragePermission(new PermissionUtil.storagePermissionCallback() {
            @Override
            public void onPermission(Boolean RESULT) {
                if(RESULT){
                    if (pickPhotoIntent.resolveActivity(activity.getPackageManager()) != null) {
                        activity.startActivityForResult(pickPhotoIntent, REQUEST_GALLERY_PICK);
                    }
                }
            }
        });

    }
    public byte[] handleImageActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        Bitmap imageBitmap = null;
        final byte[][] compressedImage = {null};

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAPTURE) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    imageBitmap = (Bitmap) extras.get("data");
                }
            } else if (requestCode == REQUEST_GALLERY_PICK) {
                Uri selectedImageUri = data.getData();
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), selectedImageUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (imageBitmap != null) {
            ImageCompressor imageCompressor = new ImageCompressor(context, quality, imageBitmap);

            imageCompressor.compressImage(new ImageCompressor.CompressorCallback() {
                @Override
                public void onCompressed(byte[] byteArray) {
                    compressedImage[0] = byteArray;
                }
            });
        }

        return compressedImage[0];
    }

}
