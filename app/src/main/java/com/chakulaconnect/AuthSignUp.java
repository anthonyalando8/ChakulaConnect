package com.chakulaconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class AuthSignUp extends AppCompatActivity {
    private LinearLayout signInLink;
    private EditText etUsername, etEmail, etPassword, etPasswordR;
    private MaterialButton btnSignUp;
    private TextView tvError;
    private RadioGroup rgAccountType, rgUserRole;

    private FirebaseUser currentUser;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private HashMap<String, String> accountDetails = new HashMap<>();
    private HashMap<String, Object> moreInfo = new HashMap<>();
    private HashMap<String, Boolean> account_role = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null){
           getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_auth_sign_up);
        // hide the  status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        auth = FirebaseAuth.getInstance();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        signInLink = findViewById(R.id.signInLink);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPasswordR = findViewById(R.id.etPasswordR);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvError = findViewById(R.id.tvError);
        rgAccountType = findViewById(R.id.rgAccType);
        rgUserRole = findViewById(R.id.rgRole);

        signInLink.setOnClickListener(v ->{
            Intent signUpIntent = new Intent(this, AuthLogin.class);
            signUpIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(signUpIntent);
        });

        //on clicking sign up button
        btnSignUp.setOnClickListener(v->{
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String password_r = etPasswordR.getText().toString().trim();

            int rgAccTypeId = rgAccountType.getCheckedRadioButtonId();

            if(rgAccTypeId != -1){
                RadioButton rbAccountType = findViewById(rgAccTypeId);
                account_role.put(rbAccountType.getText().toString(), true);
            }
            int rgRoleId = rgUserRole.getCheckedRadioButtonId();
            if(rgRoleId != -1){
                RadioButton rbUserRole = findViewById(rgRoleId);
                account_role.put(rbUserRole.getText().toString(), true);
            }

            if(username.isEmpty() | email.isEmpty() | password.isEmpty() | password_r.isEmpty() | account_role.size() < 2 ){
                tvError.setVisibility(View.VISIBLE);
                tvError.setText("All fields required!");
            }else{
                tvError.setVisibility(View.GONE);
                if(validate(username, email, password, password_r)){
                    createUserEmailAuth(username,email, password_r);
                }
            }
        });
    }

    private boolean validate(String USERNAME, String EMAIL, String PASSWORD, String PASSWORD_R){
        if(!isValidUsername(USERNAME)){
            etUsername.setError("Invalid name");
            return false;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(EMAIL).matches()){
            etEmail.setError("Invalid email");
           return false;
        }
        if(PASSWORD.length() < 6){
            etPassword.setError("Weak password");
            return false;
        }else{
            if(!PASSWORD_R.equals(PASSWORD)){
                etPasswordR.setError("Password not matching");
                return false;
            }
        }
        return true;
    }
    public boolean isValidUsername(String username) {
        // Check length requirements (3 to 30 characters)
        if (username.length() < 3 || username.length() > 30) {
            return false;
        }

        // Check allowed characters (alphanumeric, underscore, hyphen)
        if (!username.matches("^[a-zA-Z\\d _-]+$")) {
            return false;
        }

        // Check for reserved usernames (optional)
        List<String> reservedUsernames = Arrays.asList("admin", "root");
        return !reservedUsernames.contains(username.toLowerCase());
    }

    private void createUserEmailAuth(String DISPLAY_NAME, String EMAIL, String PASSWORD){
        AtomicBoolean userCreated = new AtomicBoolean(false);
        AtomicReference<String> errorMessage = new AtomicReference<>("");
        AlertDialog alertDialog = Progress.createAlertDialog(this, "Please wait...");
        alertDialog.show();
        auth.createUserWithEmailAndPassword(EMAIL, PASSWORD)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        currentUser = auth.getCurrentUser();
                        runOnUiThread(()->{
                            TextView txtLoadingInfo = alertDialog.findViewById(R.id.txtLoading);
                            if(txtLoadingInfo != null){
                                txtLoadingInfo.setText("Creating avatar...");
                            }

                            //creating profile picture
                            int sizeInPixels = 200;
                            Bitmap avatarBitmap = AvatarGenerator.generateAvatar(DISPLAY_NAME, sizeInPixels);
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            avatarBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                            byte[] data = outputStream.toByteArray();

                            String storagePath = "Images/"+currentUser.getUid()+"/Avatars/"+System.currentTimeMillis()+".png";
                            storageReference.child(storagePath).putBytes(data)
                                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task1) {
                                            if (task1.isSuccessful()){
                                                TextView txtLoadingInfo = alertDialog.findViewById(R.id.txtLoading);
                                                if(txtLoadingInfo != null){
                                                    txtLoadingInfo.setText("Downloading avatar...");
                                                }
                                                task1.getResult().getStorage().getDownloadUrl()
                                                        .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Uri> task2) {
                                                                if(task2.isSuccessful()){
                                                                    String imageUriString = task2.getResult().toString();
                                                                    Uri imageUri = task2.getResult();
                                                                    TextView txtLoadingInfo = alertDialog.findViewById(R.id.txtLoading);
                                                                    if(txtLoadingInfo != null){
                                                                        txtLoadingInfo.setText("Updating profile...");
                                                                    }
                                                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                                            .setDisplayName(DISPLAY_NAME)
                                                                            .setPhotoUri(imageUri).build();

                                                                    currentUser.updateProfile(profileUpdates)
                                                                            .addOnCompleteListener(task3->{
                                                                                if(task3.isSuccessful()){
                                                                                    if(txtLoadingInfo != null){
                                                                                        txtLoadingInfo.setText("Saving information...");
                                                                                    }
                                                                                    Long JOIN_LONG = System.currentTimeMillis();
                                                                                    String joinDate = Long.toString(JOIN_LONG);
                                                                                    accountDetails.put("userId", currentUser.getUid());
                                                                                    accountDetails.put("userName", DISPLAY_NAME);
                                                                                    accountDetails.put("email", EMAIL);
                                                                                    accountDetails.put("imageUri", imageUriString);
                                                                                    accountDetails.put("coverUri", "");
                                                                                    accountDetails.put("joinDate", joinDate);

                                                                                    account_role.put("complete", false);

                                                                                    moreInfo.put("phone", "");
                                                                                    moreInfo.put("country", "");
                                                                                    moreInfo.put("county", "");
                                                                                    moreInfo.put("address", "");
                                                                                    moreInfo.put("moreInfo", "");
                                                                                    moreInfo.put("location", "");
                                                                                    HashMap<String, UserActivityModel> userActivityModelHashMap = new HashMap<>();
                                                                                    userActivityModelHashMap.put(joinDate, new UserActivityModel("New Account Created", "Welcome aboard! Your account is ready.", currentUser.getUid(),joinDate, JOIN_LONG));

                                                                                    UserModel userModel = new UserModel(accountDetails, account_role, moreInfo, userActivityModelHashMap);

                                                                                    DatabaseReference databaseReference;

                                                                                    databaseReference = FirebaseDatabase.getInstance().getReference();

                                                                                    databaseReference.child("Users").child(currentUser.getUid()).setValue(userModel)
                                                                                            .addOnCompleteListener(task4 -> {
                                                                                                if(task4.isSuccessful()){

                                                                                                    //verify user email address

                                                                                                    String url = "https://chakulaconnect.page.link/home";
                                                                                                    ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                                                                                                            .setUrl(url)
                                                                                                            // The default for this is populated with the current android package name.
                                                                                                            .setAndroidPackageName("com.chakulaconnect", false, null)
                                                                                                            .build();
                                                                                                    if(txtLoadingInfo != null){
                                                                                                        txtLoadingInfo.setText("Sending verification link...");
                                                                                                    }
                                                                                                    currentUser.sendEmailVerification(actionCodeSettings)
                                                                                                            .addOnCompleteListener(task5->{
                                                                                                                if (task5.isSuccessful()){
                                                                                                                    alertDialog.dismiss();
                                                                                                                    userCreated.set(true);
                                                                                                                    Toast.makeText(AuthSignUp.this, "Check mailbox for verification link", Toast.LENGTH_SHORT).show();
                                                                                                                    userCreationResult(userCreated.get(), errorMessage.get());
                                                                                                                }
                                                                                                            }).addOnFailureListener(e->{
                                                                                                                alertDialog.dismiss();
                                                                                                                tvError.setText(e.getMessage());
                                                                                                            });
                                                                                                }
                                                                                            }).addOnFailureListener(e -> {
                                                                                                Toast.makeText(AuthSignUp.this, "Database failure", Toast.LENGTH_SHORT).show();
                                                                                            });

                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });


                        });



                    }
                }).addOnFailureListener(e-> {
                    userCreated.set(false);
                    alertDialog.dismiss();
                    userCreationResult(userCreated.get(), e.getMessage());
                });
    }
    public void userCreationResult(boolean RESULT, String ERROR){
        if(RESULT){
            //if user is created successfully
            Intent mainActivity = new Intent(this, AccountVerification.class);
            mainActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainActivity);
        }else{
            tvError.setText(ERROR);
            tvError.setVisibility(View.VISIBLE);
        }
    }
    private boolean isUser(){
        if(currentUser == null){
            return false;
        }
        return true;
    }
}