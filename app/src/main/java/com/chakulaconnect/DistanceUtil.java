package com.chakulaconnect;

import android.content.Context;

public class DistanceUtil {
    Context context;

    public DistanceUtil(Context context) {
        this.context = context;
    }
    public interface distanceUtilCallback{
        void onDistanceCalculated(double distance_km);
    }
    public void calculateDistance(distanceUtilCallback distanceUtilCallback, double lat1, double lon1, double lat2, double lon2){
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        distanceUtilCallback.onDistanceCalculated(c);
    }
}
