package com.chakulaconnect;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import java.util.Random;

public class AvatarGenerator {

    /**
     * This method generates an avatar (bitmap image) based on the first letter of a given name.
     *
     * @param name           The name from which the initials will be extracted.
     * @param size           The size (width and height) of the avatar in pixels.
     * @return A bitmap containing the circular avatar with the initials text.
     */
    public static Bitmap generateAvatar(String name, int size) {
        // Create a new bitmap with the specified size and configuration
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        int backgroundColor = getRandomColor(); // Set your desired background color for the avatar
        int textColor = getDifferentColor(getRandomColor()); // Set your desired text color for the initials
        // Create a new canvas to draw on the bitmap
        Canvas canvas = new Canvas(bitmap);

        // Initialize a Paint object to set properties for drawing
        Paint paint = new Paint();
        paint.setAntiAlias(true); // Enable anti-aliasing for smoother edges

        // Set a bold typeface for the text
        Typeface typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
        paint.setTypeface(typeface);

        paint.setColor(backgroundColor); // Set the background color of the circular avatar
        paint.setStyle(Paint.Style.FILL); // Set the paint style to fill

        // Draw a circle on the canvas to create the circular avatar
        float radius = size / 2f;
        canvas.drawCircle(radius, radius, radius, paint);

        // Calculate the text size based on the size of the avatar
        int textSize = size / 3;
        paint.setTextSize(textSize); // Set the text size
        paint.setTextAlign(Paint.Align.CENTER); // Align the text in the center

        // Set the text color for the initials
        paint.setColor(textColor);

        // Calculate the vertical position of the initials to center them in the avatar
        float textY = size / 2f - ((paint.descent() + paint.ascent()) / 2);

        // Draw the initials on the canvas at the center of the avatar
        canvas.drawText(getInitials(name), radius, textY, paint);

        return bitmap; // Return the generated bitmap (avatar)
    }

    /**
     * This method extracts the initials from a given name.
     *
     * @param name The name from which the initials will be extracted.
     * @return The initials as a string in uppercase.
     */
    private static String getInitials(String name) {
        // Create a StringBuilder to store the initials
        int count = 0;
        StringBuilder initials = new StringBuilder();

        // Split the name into words using whitespace as a delimiter
        String[] words = name.split("\\s+");

        // Iterate through each word to extract the first character and append it to the initials
        for (String word : words) {
            if (!word.isEmpty()) {
                initials.append(word.charAt(0));
                count++;
            }
            if (count == 2){
                break;
            }
        }

        // Convert the initials to uppercase and return as a string
        return initials.toString().toUpperCase();
    }
    private static int getRandomColor() {
        Random random = new Random();
        // Generate random color with full opacity (alpha = 255)
        return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }
    private static int getDifferentColor(int colorToAvoid) {
        int textColor;
        do {
            textColor = getRandomColor();
        } while (textColor == colorToAvoid); // Keep generating until the text color is different from the background color
        return textColor;
    }

}

