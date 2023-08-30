package com.chakulaconnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkStateReceiver extends BroadcastReceiver {
    private NetworkStateListener networkStateListener;
    private boolean isConnected = false;

    public NetworkStateReceiver(NetworkStateListener networkStateListener) {
        this.networkStateListener = networkStateListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (networkStateListener != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connectivityManager != null) {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                boolean isConnectedNow = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

                if (isConnectedNow && !isConnected) {
                    isConnected = true;
                    networkStateListener.onNetworkConnected();
                } else if (!isConnectedNow && isConnected) {
                    isConnected = false;
                    networkStateListener.onNetworkDisconnected();
                }
            }
        }
    }

    public interface NetworkStateListener {
        void onNetworkConnected();
        void onNetworkDisconnected();
    }
}
