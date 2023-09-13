package com.chakulaconnect;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.flexbox.FlexboxLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomFlexBox extends FlexboxLayout {
    public CustomFlexBox(Context context) {
        super(context);
    }

    public CustomFlexBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int newWidth = MeasureSpec.makeMeasureSpec(parentWidth, MeasureSpec.EXACTLY);
        super.onMeasure(newWidth, heightMeasureSpec);
    }

    public void setData(ArrayList<AboutSecOneModel> dataList) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (AboutSecOneModel data : dataList) {
            // Inflate your custom layout
            View customView = inflater.inflate(R.layout.custom_flex_box_single, null);

            // Find and set data on views within the custom layout
            TextView textView = customView.findViewById(R.id.txtAboutTitle);
            ConstraintLayout clMain = customView.findViewById(R.id.clView);
            CircleImageView circleImageView = customView.findViewById(R.id.civAboutIcon);
            textView.setText(data.getTitle());
            Picasso.get().load(data.getImageUri()).into(circleImageView);

            clMain.setOnClickListener(v->{
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(data.getTitle());
                ImageView iconImageView = new ImageView(getContext());
                Picasso.get().load(data.getImageUri()).into(iconImageView);
                builder.setIcon(iconImageView.getDrawable());
                builder.setMessage(data.getDescription());
                builder.setPositiveButton("Close", (dialog, which)->{
                    dialog.dismiss();
                });
                builder.create().show();
            });
            // Create and set layout parameters for customView
            FlexboxLayout.LayoutParams layoutParams = new FlexboxLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, // Set width (you can change this to your requirements)
                    FlexboxLayout.LayoutParams.WRAP_CONTENT  // Set height (you can change this to your requirements)
            );
            layoutParams.setMargins(4,4,0,4);
            layoutParams.setFlexGrow(1); // Set flexGrow to 1 to share available space equally

            //Set layout parameters for customView
            customView.setLayoutParams(layoutParams);
            // Add the custom view to the Flexbox container
            addView(customView);
        }
    }
}
