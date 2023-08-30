package com.chakulaconnect;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountVerification extends AppCompatActivity implements FirebaseAuth.AuthStateListener{
    private MaterialButton btnRequestVerification;
    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;
    TextView txtInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_verification);

        txtInfo = findViewById(R.id.info);
        btnRequestVerification = findViewById(R.id.btnRequestVerifyCode);
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        txtInfo.setText("Dear ".concat((currentUser != null) ? currentUser.getDisplayName() : "User").concat(",\n")+
                "\n" +
                "Thank you for signing up! Your account has not been verified yet.\n" +
                "\n" +
                "To complete the verification process, please check your email inbox for a verification link. Click on the link provided in the email to verify your account. \n" +
                "\n" +
                "If you haven't received the verification email, you can request a new one by clicking on the button below:\n" +
                "\n" +
                "Once your account is verified, you'll be able to access all the features and benefits of our app.\n" +
                "\n" +
                "Thank you for your cooperation.");

        btnRequestVerification.setOnClickListener(v->{
            AlertDialog alertDialog = Progress.createAlertDialog(this, "Please wait...");
            alertDialog.show();
            String url = "https://chakulaconnect.page.link/home";
            ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                    .setUrl(url)
                    // The default for this is populated with the current android package name.
                    .setAndroidPackageName("com.chakulaconnect", false, null)
                    .build();
            currentUser.sendEmailVerification(actionCodeSettings)
                    .addOnCompleteListener(task->{
                        if (task.isSuccessful()){
                            alertDialog.dismiss();
                            Toast.makeText(this, "Check inbox to verify email", Toast.LENGTH_LONG).show();
                            txtInfo.setText("An email has been sent to your inbox with a verification link.\n" +
                                    "To complete the account verification process, please check your email and click on the verification link provided."+
                                    "\n" +
                                    "If you don't find the verification email in your inbox, please check your spam or junk folder as it might have been filtered there.\n" +
                                    "If you encounter any issues or need further assistance, please don't hesitate to contact our support team at support@chakulaconnect.com.");

                            btnRequestVerification.setVisibility(View.GONE);
                        }
                    }).addOnFailureListener(e->{
                        alertDialog.dismiss();
                        txtInfo.setText("Error occurred try again later");
                    });
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(this);
        FirebaseAuth.getInstance().signOut();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseAuth.getInstance().signOut();
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseAuth.getInstance().signOut();
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null && user.isEmailVerified()) {
            txtInfo.setText("Verified. Redirecting...");
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            boolean onBoardViewed = sharedPreferences.getBoolean("on_board_viewed", false);
            new Handler().postDelayed(()->{
                Intent mainIntent = (onBoardViewed) ? new Intent(AccountVerification.this.getApplicationContext(), MainActivity.class) : new Intent(AccountVerification.this.getApplicationContext(), OnBoarding.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                AccountVerification.this.startActivity(mainIntent);
                finish();
            }, 1000);
        }else{
            if (user == null){
                Intent loginIntent = new Intent(this, AuthLogin.class);
                startActivity(loginIntent);
                finish();
            }

        }
    }
}