package com.chakulaconnect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class ImageCompressor {
    private Context context;
    private int imageQuality;
    private Bitmap originalBitmap;

    public ImageCompressor(Context context, int imageQuality, Bitmap originalBitmap) {
        this.context = context;
        this.imageQuality = imageQuality;
        this.originalBitmap = originalBitmap;
    }

    public interface CompressorCallback {
        void onCompressed(byte[] byteArray);
    }

    public void compressImage(CompressorCallback callback) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        originalBitmap.compress(Bitmap.CompressFormat.JPEG, imageQuality, outputStream);

        byte[] byteArray = outputStream.toByteArray();
        callback.onCompressed(byteArray);
    }
}
