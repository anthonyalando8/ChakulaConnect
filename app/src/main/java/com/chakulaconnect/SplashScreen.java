package com.chakulaconnect;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreen extends AppCompatActivity implements NetworkStateReceiver.NetworkStateListener {
        private ProgressBar pbLoadingMain;
        private FirebaseAuth auth;
        private FirebaseUser currentUser;
        private NetworkStateReceiver networkStateReceiver;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_splash_screen);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        auth = FirebaseAuth.getInstance();

        pbLoadingMain = findViewById(R.id.pbLoadingMain);
        networkStateReceiver = new NetworkStateReceiver(this);
        registerNetworkStateReceiver();
        new Handler().postDelayed(()->{


            Intent mainIntent;
            if(isUser()){
                currentUser = auth.getCurrentUser();
                sharedPreferences = getSharedPreferences(currentUser.getUid()+"_pref", MODE_PRIVATE);
                boolean onBoardViewed = sharedPreferences.getBoolean("on_board_viewed", false);
                if(!currentUser.isEmailVerified()){
                    mainIntent = new Intent(this, AccountVerification.class);
                }else {
                    mainIntent = (onBoardViewed) ? new Intent(this, MainActivity.class) : new Intent(this, OnBoarding.class);
                }

            }else{
                mainIntent =  new Intent(this, AuthLogin.class);
            }
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
        }, 1000);

        DeviceModel deviceModel = new DeviceModel(this);
        deviceModel.getBuildInfo(new DeviceModel.DeviceModelCallback() {
            @Override
            public void deviceInfo(String buildInfo) {
                System.out.println(buildInfo);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterNetworkStateReceiver();
    }

    private boolean isUser(){
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null){
            return false;
        }
        return true;
    }

    @Override
    public void onNetworkConnected() {
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNetworkDisconnected() {
        Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
    }
    private void registerNetworkStateReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkStateReceiver, intentFilter);
    }

    private void unregisterNetworkStateReceiver() {
        if (networkStateReceiver != null) {
            unregisterReceiver(networkStateReceiver);
        }
    }

}