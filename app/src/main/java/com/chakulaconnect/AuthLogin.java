package com.chakulaconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class AuthLogin extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private TextView tvError;
    private FirebaseAuth auth;
    private FirebaseUser user;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
            getSupportActionBar().setSubtitle("Login");
        }
        setContentView(R.layout.activity_auth_login);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();

        LinearLayout signUpLink = findViewById(R.id.signUpLink);
        tvError = findViewById(R.id.tvError);
        MaterialButton btnSignIn = findViewById(R.id.btnLogin);
        etEmail = findViewById(R.id.txtEmail);
        etPassword = findViewById(R.id.txtPassword);

        TextView txtForgotPassword = findViewById(R.id.txtForgotPassword);

        signUpLink.setOnClickListener(v->{
            Intent signUpIntent = new Intent(this, AuthSignUp.class);
            signUpIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(signUpIntent);
        });

        btnSignIn.setOnClickListener(v->{
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if(validate(email, password)){
                loginUser(email, password);
            }
        });
        txtForgotPassword.setOnClickListener(v->{
            passwordRecoveryDialog();
        });

        etPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP){
                    btnSignIn.performClick();
                }
                return false;
            }
        });
    }
    public void loginUser(String EMAIL, String PASSWORD){
        AtomicBoolean isSuccess = new AtomicBoolean(false);
        AtomicReference<String> error = new AtomicReference<>("");
        AlertDialog alertDialog = Progress.createAlertDialog(this, "Please Wait...");
        alertDialog.show();
        auth.signInWithEmailAndPassword(EMAIL, PASSWORD).addOnCompleteListener(task -> {
            if(task.isSuccessful()){

                DeviceModel deviceModel = new DeviceModel(this);
                deviceModel.getBuildInfo(new DeviceModel.DeviceModelCallback() {
                    @Override
                    public void deviceInfo(String buildInfo) {
                        Long activityTime = System.currentTimeMillis();
                        reference.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child("activity").child(Long.toString(activityTime))
                                .setValue(new UserActivityModel("Login","Device: ".concat(buildInfo),
                                        FirebaseAuth.getInstance().getCurrentUser().getUid(),Long.toString(activityTime),activityTime))
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            isSuccess.set(true);
                                            Toast.makeText(AuthLogin.this, "Updates made", Toast.LENGTH_SHORT).show();
                                            handleLoginResult(isSuccess.get(), error.get());
                                            alertDialog.dismiss();
                                        }else{
                                            alertDialog.dismiss();
                                        }

                                    }
                                }).addOnFailureListener(e->{
                                    alertDialog.dismiss();
                                    handleLoginResult(false, e.getMessage());
                                    Toast.makeText(AuthLogin.this, "Something went", Toast.LENGTH_SHORT).show();
                                });
                    }
                });
            }
        }).addOnFailureListener(e->{
            isSuccess.set(false);
            error.set(e.getMessage());
            alertDialog.dismiss();
            handleLoginResult(isSuccess.get(), error.get());
        });
    }
    public void handleLoginResult(boolean RESULT, String ERROR){
        if(RESULT){
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Intent mainActivity;
            if (user != null) {
                if(user.isEmailVerified()){
                    SharedPreferences sharedPreferences = getSharedPreferences(user.getUid()+"_pref", MODE_PRIVATE);
                    boolean onBoardViewed = sharedPreferences.getBoolean("on_board_viewed", false);
                    if(onBoardViewed){
                        mainActivity = new Intent(this, MainActivity.class);
                    }else{
                        mainActivity = new Intent(this, OnBoarding.class);
                    }
                }else {
                    mainActivity = new Intent(this, AccountVerification.class);
                }
                mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainActivity);
            }

        }else{
            tvError.setText(ERROR);
            tvError.setVisibility(View.VISIBLE);
        }
    }
    private boolean validate(String EMAIL, String PASSWORD){
        if(EMAIL.isEmpty() | PASSWORD.isEmpty()){
            tvError.setText("All fields required!");
            tvError.setVisibility(View.VISIBLE);
            return false;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(EMAIL).matches()){
            etEmail.setError("Invalid email");
            return false;
        }
        tvError.setVisibility(View.GONE);
        return true;
    }

    private void passwordRecoveryDialog(){
        View view = LayoutInflater.from(AuthLogin.this).inflate(R.layout.password_recovery, null);
        EditText etEmailRecover = view.findViewById(R.id.etEmailRecovery);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setView(view);
        builder.setTitle("Recover password");
        builder.setPositiveButton("Recover", (dialog, which)->{
            String emailRecover = etEmailRecover.getText().toString().trim();
            if(!emailRecover.isEmpty()){
                recoverPassword(emailRecover);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which)->{
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void recoverPassword(String EMAIL){
        AlertDialog alertDialog = Progress.createAlertDialog(this, "Requesting...");
        alertDialog.show();
        auth.sendPasswordResetEmail(EMAIL).addOnCompleteListener(task->{
            if(task.isSuccessful()){
                Toast.makeText(this, "Check your email", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            }else {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            }
        });
    }
}