package com.chakulaconnect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity {
    ImageUtil imageUtil;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference databaseReference;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    TextView btn_update_dp, btn_update_cover;
    ImageView ivCover;
    CircleImageView civProfilePic;
    View selectedView;
    String coverPath, avatarPath;
    SharedPreferences sharedPreferences;
    Gson gson;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        btn_update_cover = findViewById(R.id.btn_update_cover);
        btn_update_dp = findViewById(R.id.btn_update_profile_pic);
        ivCover = findViewById(R.id.iv_cover_image);
        civProfilePic = findViewById(R.id.civ_user_image);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        gson = new Gson();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        imageUtil = new ImageUtil(this, this,80);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(isUser()){
            sharedPreferences = getSharedPreferences(user.getUid()+"_pref", MODE_PRIVATE);

            String userData = sharedPreferences.getString(user.getUid()+"_data", null);
            if(userData != null){
                UserModel userModel = gson.fromJson(userData, UserModel.class);
                if(userModel.getAccountDetails().containsKey("coverUri") && !userModel.getAccountDetails().get("coverUri").isEmpty()){
                    Picasso.get().load(userModel.getAccountDetails().get("coverUri")).into(ivCover);
                }
            }
            Picasso.get()
                    .load(user.getPhotoUrl())
                    .into(civProfilePic);

        }
        btn_update_dp.setOnClickListener(v->{
            selectedView = (CircleImageView) civProfilePic;
            imageUtil.createImageDialog();
        });
        btn_update_cover.setOnClickListener(v->{
            selectedView = (ImageView) ivCover;
            imageUtil.createImageDialog();
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == imageUtil.REQUEST_CAPTURE || requestCode == imageUtil.REQUEST_GALLERY_PICK){
            byte[] byteArray = imageUtil.handleImageActivityResult(this, requestCode, resultCode, data);
            if(data != null){
                Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                if (selectedView instanceof CircleImageView) {
                    avatarPath = "Images/"+user.getUid()+"/Avatars/"+System.currentTimeMillis()+".jpg";
                    ((CircleImageView) selectedView).setImageBitmap(bitmap); // Set bitmap to CircleImageView
                    HashMap<String, Boolean> process = new HashMap<>();
                    process.put("Avatar", true);
                    uploadImage(byteArray, avatarPath, "Uploading avatar...", process);
                } else if (selectedView instanceof ImageView) {
                    coverPath = "Images/"+user.getUid()+"/Covers/"+System.currentTimeMillis()+".jpg";
                    HashMap<String, Boolean> process = new HashMap<>();
                    process.put("Cover", true);
                    ((ImageView) selectedView).setImageBitmap(bitmap); // Set bitmap to ImageView
                    uploadImage(byteArray, coverPath, "Uploading cover...", process);
                }
            }else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public boolean isUser(){
        if(user != null){
            return true;
        }
        return false;
    }
    public void uploadImage(byte[] byteArray, String path, String alertMessage, HashMap<String, Boolean> processType){
        AlertDialog alertDialog = Progress.createAlertDialog(this, alertMessage);
        alertDialog.show();
        storageReference.child(path).putBytes(byteArray)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            TextView alertMessage = alertDialog.findViewById(R.id.txtLoading);
                            if(alertMessage != null){
                                alertMessage.setText("Downloading link");
                            }
                            task.getResult().getStorage().getDownloadUrl()
                                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if(task.isSuccessful()){
                                                Uri imageUri = task.getResult();
                                                String imageUriStr = imageUri.toString();
                                                if(processType.containsKey("Avatar")){
                                                    alertMessage.setText("Updating profile...");
                                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                            .setPhotoUri(imageUri).build();
                                                    user.updateProfile(profileUpdates)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        uploadUri("Users/"+user.getUid()+"/accountDetails/imageUri", imageUriStr, "Saving information", alertDialog, alertMessage);
                                                                    }
                                                                }
                                                            });
                                                } else if (processType.containsKey("Cover")) {
                                                    uploadUri("Users/"+user.getUid()+"/accountDetails/coverUri", imageUriStr, "Saving information", alertDialog, alertMessage);
                                                }
                                            }
                                        }
                                    });
                        }
                    }
                }).addOnFailureListener(e->{
                    alertDialog.dismiss();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    public void uploadUri(String path, String Uri, String dialogMsg, AlertDialog alertDialog, TextView alertMessage){
        if(alertMessage != null){
            alertMessage.setText(dialogMsg);
        }
        databaseReference.child(path)
                .setValue(Uri).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            alertDialog.dismiss();
                            Toast.makeText(EditProfile.this, "Updated", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(e->{
                    alertDialog.dismiss();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
}