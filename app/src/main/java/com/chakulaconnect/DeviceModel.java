package com.chakulaconnect;

import android.content.Context;
import android.os.Build;

public class DeviceModel {
    Context context;

    public DeviceModel(Context context) {
        this.context = context;
    }
    public interface DeviceModelCallback{
        void deviceInfo(String buildInfo);
    }
    public void getBuildInfo(DeviceModelCallback modelCallback){
        String manufacturer = Build.MANUFACTURER; // Manufacturer name
        String model = Build.MODEL; // Model name
        String device = Build.DEVICE; // Device name
        String product = Build.PRODUCT; // Product name
        String display = Build.DISPLAY; // Display information
        modelCallback.deviceInfo(manufacturer.concat("/"+model).concat("/"+device).concat("/"+product).concat("/"+display));
    }
}
