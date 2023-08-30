package com.chakulaconnect;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import de.hdodenhof.circleimageview.CircleImageView;

public class OnBoarding extends AppCompatActivity {
    private TextView txtTitle, txtContent, txtContentNo;
    private ImageButton btnNext;
    private final ArrayList<OnBoardModel> aboutUs = new ArrayList<>();
    private ImageView ivImageInfo;
    private ConstraintLayout layoutMain;
    AtomicInteger currentContent;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    FirebaseAuth auth;
    FirebaseUser currentUser;
    String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("About app");
        }
        setContentView(R.layout.activity_on_boarding);

        txtContent = findViewById(R.id.txtPageContent);
        txtTitle = findViewById(R.id.txtPageInfo);
        btnNext = findViewById(R.id.btnNext);
        ImageButton btnPrevious = findViewById(R.id.btnPrevious);
        ivImageInfo = findViewById(R.id.ivImageInfo);
        txtContentNo = findViewById(R.id.txtContentNo);
        layoutMain = findViewById(R.id.layoutMain);
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        if(isUser()){
            sharedPreferences = getSharedPreferences(currentUser.getUid()+"_pref", MODE_PRIVATE);
            editor = sharedPreferences.edit();
            userName = currentUser.getDisplayName();
        }



        aboutUs.add(new OnBoardModel("Chakula Connect is a revolutionary mobile application that aims to combat food waste and alleviate hunger " +
                "in communities. Our app acts as a bridge, connecting generous food donors, including restaurants, supermarkets, " +
                "caterers, and individuals, with organizations and individuals in need, such as charities, shelters, and vulnerable " +
                "communities.", R.drawable.light));
        aboutUs.add(new OnBoardModel("Mission: \n" +
                "At Chakula Connect, we are dedicated to creating a world where surplus edible food does not go to waste but " +
                "instead reaches those who are food-insecure. Our primary goal is to rescue food that would otherwise be discarded " +
                "and direct it to those who need it most.", R.drawable.mission));
        aboutUs.add(new OnBoardModel("How It Works:\n" +
                "Using Chakula Connect is simple and impactful. Food donors can list surplus food items available for " +
                "donation through our intuitive and user-friendly interface. Meanwhile, food recipients can browse through these " +
                "listings and claim the items they require. Through our efficient platform, we facilitate the seamless transfer of " +
                "surplus food, ensuring that it gets to those who need it quickly and safely.", R.drawable.how));
        aboutUs.add(new OnBoardModel("Impact:\n" +
                "By utilizing Chakula Connect, food donors can significantly reduce food wastage promoting responsible consumption " +
                "and production practices (SDG 12). At the same time, food recipients can enjoy access to nutritious meals helping " +
                "them meet their basic needs and improving their overall well-being,(SDG 2: Zero Hunger)", R.drawable.impact));
        aboutUs.add(new OnBoardModel("Dear ".concat(userName).concat(",\n") +
                "\n" +
                "Thank you for installing Chakula Connect! Your support helps fight hunger and food waste. Together, we'll create a better world, one rescued meal at a time.\n" +
                "\n" +
                "Welcome aboard!\n" +
                "The Chakula Connect Team", R.drawable.thanks));

        currentContent = new AtomicInteger(sharedPreferences.getInt("current_about_index", 0));

        changeContent(currentContent.get());
        btnNext.setOnClickListener(v->{
            currentContent.set(currentContent.get() + 1);
            if(currentContent.get() == aboutUs.size()){
                editor.putBoolean("on_board_viewed", true);
                editor.apply();

                Intent mainActivity = new Intent(this, MainActivity.class);
                mainActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainActivity);
            }else{
                changeContent(currentContent.get());
                saveCurrentContent(currentContent.get());
            }
        });
        btnPrevious.setOnClickListener(v->{
            if(currentContent.get() >= 0 && currentContent.get() != 0){
                currentContent.set(currentContent.get() - 1);
                changeContent(currentContent.get());
                saveCurrentContent(currentContent.get());
            }

        });
    }

    private void changeContent(int index){
        btnNext.setImageDrawable((index != aboutUs.size() - 1) ? getResources().getDrawable(R.drawable.nav_next, getTheme()) : getResources().getDrawable(R.drawable.done, getTheme()));
        OnBoardModel item = aboutUs.get(index);
        layoutMain.animate().alpha(0.0f)
                .setDuration(200)
                .withEndAction(() -> {
                    txtContent.setText(item.getContentText());
                    txtContentNo.setText(currentContent.get() + 1 + "/" + aboutUs.size());
                    Picasso.get().load(item.getImageId()).into(ivImageInfo);
                    layoutMain.animate().alpha(1.0f)
                            .setDuration(200).start();
                }).start();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.on_board_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.nav_skip){
            Intent mainActivity = new Intent(this, MainActivity.class);
            mainActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainActivity);
        }
        return super.onOptionsItemSelected(item);
    }
    public void saveCurrentContent(int contentIndex){
        editor.putInt("current_about_index", contentIndex);
        editor.apply();
    }
    private boolean isUser(){
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            return true;
        }
        return false;
    }

}