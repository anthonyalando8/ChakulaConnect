package com.chakulaconnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.net.InetAddress;

public class InternetConnectionChecker extends BroadcastReceiver {
    Context context;
    boolean connected = false;

    public InternetConnectionChecker(Context context) {
        this.context = context;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        connected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
    public boolean isConnected(){
        return connected;
    }
}
