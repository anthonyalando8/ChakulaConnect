package com.chakulaconnect;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener,
        NetworkStateReceiver.NetworkStateListener {

    FirebaseUser user;
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    NavigationView navigationView;
    DrawerLayout drawerLayout;

    String userId, userName;
    TextView nav_user_name;
    CircleImageView nav_user_image;
    ConstraintLayout nav_user_account;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Boolean isDonor = false, isRecipient = false, isComplete = false, isIndividual = false, isOrganisation = false;
    Toolbar toolbar;
    ImageButton nav_menu, nav_add_post;
    CircleImageView nav_account;
    private NetworkStateReceiver networkStateReceiver;
    Long joinDate;
    Gson gson;
    ImageUtil imageUtil;
    InternetConnectionChecker connectionChecker;
    public ViewPager2 viewPager2Main;
    boolean isActivityCreated = false;
    ValueEventListener userNodeValueEventListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // drawer layout instance to toggle the menu icon to open
        // drawer and back button to close drawer
        drawerLayout = findViewById(R.id.drawer_layout);


        navigationView = findViewById(R.id.drawer_navigation);
        nav_account = findViewById(R.id.nav_account_icon);
        nav_menu = findViewById(R.id.nav_menu_icon);
        viewPager2Main = findViewById(R.id.viewPagerMain);
        nav_add_post = findViewById(R.id.nav_add_post);
        View navHeader = navigationView.getHeaderView(0);

        nav_user_image = navHeader.findViewById(R.id.nav_user_image);
        nav_user_name = navHeader.findViewById(R.id.txt_nav_user_name);
        nav_user_account = navHeader.findViewById(R.id.header_main_layout);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        isActivityCreated = true;
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        networkStateReceiver = new NetworkStateReceiver(this);

        registerNetworkStateReceiver();




        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        gson = new Gson();
        connectionChecker = new InternetConnectionChecker(this);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        navHeader.setOnClickListener(v->{
            if(isUser()){
                loadUserProfile();
            }

        });

        if(isUser()){
            userId = user.getUid();
            userName = user.getDisplayName();
            sharedPreferences = getSharedPreferences(user.getUid()+"_pref", MODE_PRIVATE);
            String userData = sharedPreferences.getString(user.getUid()+"_data", null);
            editor = sharedPreferences.edit();

            if(!connectionChecker.isConnected()){
                if(userData != null){
                    UserModel userModel = gson.fromJson(userData, UserModel.class);
                    getValues(userModel);
                    handlesAccComplete(true);
                }
            }

            loadUserPhoto();
        }
        nav_menu.setOnClickListener(v->{
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                // If the drawer is open, close it
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                // If the drawer is closed, open it
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        nav_account.setOnClickListener(v->{
            if (isUser()){
                loadUserProfile();
            }
        });

        nav_add_post.setOnClickListener(v->{
            addNewPost();
        });
        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START, false);
            if (item.getItemId() == R.id.nav_side_dashboard){
                viewPager2Main.setCurrentItem(0);
            }
            if(item.getItemId() == R.id.nav_side_logout){
                FirebaseAuth.getInstance().signOut();
            }
            if(item.getItemId() == R.id.nav_side_donors || item.getItemId() == R.id.nav_side_recipients){
                Intent usersIntent = new Intent(this, UsersActivity.class);
                usersIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(usersIntent);
            }
            if(item.getItemId() == R.id.nav_side_donate || item.getItemId() == R.id.nav_side_make_request){
                Intent donateRequest = new Intent(this, ActivityDonationInfo.class);
                donateRequest.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(donateRequest);
            }
            if(item.getItemId() == R.id.nav_side_my_donations || item.getItemId() == R.id.nav_my_side_request){
                Intent myDonations = new Intent(this, donor_donations.class);
                myDonations.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(myDonations);
            }
            if(item.getItemId() == R.id.nav_side_about){
                Intent aboutUs = new Intent(this, ActivityAboutUs.class);
                aboutUs.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(aboutUs);
            }
            return false;
        });
    }

    private void checkUpdates(String EXPECT_VERSION){
        databaseReference.child("App")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String app_version = snapshot.child("app_version").getValue(String.class);
                            String app_location = snapshot.child("app_location").getValue(String.class);
                            if(!EXPECT_VERSION.equals(app_version)) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setCancelable(false);
                                builder.setTitle("Updates");
                                builder.setMessage("Updates available");
                                builder.setPositiveButton("Update", (dialog, which) -> {
                                    PermissionUtil permissionUtil = new PermissionUtil(MainActivity.this);
                                    permissionUtil.requestStoragePermission(new PermissionUtil.storagePermissionCallback() {
                                        @Override
                                        public void onPermission(Boolean RESULT) {
                                            if (RESULT) {
                                                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(app_location));
                                                    request.setTitle("App Update");
                                                    request.setDescription("Downloading update");
                                                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "ChakulaConnect-v-" + app_version + ".apk");
                                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                                                    // Get the DownloadManager service and enqueue the request
                                                    DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                                    long downloadId = downloadManager.enqueue(request);

                                                    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                                                        @Override
                                                        public void onReceive(Context context, Intent intent) {
                                                            String action = intent.getAction();
                                                            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                                                                long completedDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                                                                // Check if the downloadId matches the one you initiated
                                                                if (completedDownloadId == downloadId) {
                                                                    // Handle the download completion here
                                                                    Toast.makeText(context, "Download completed, open downloads to install", Toast.LENGTH_SHORT).show();

                                                                    // Get the URI of the downloaded file
                                                                    Uri downloadedFileUri = downloadManager.getUriForDownloadedFile(downloadId);

                                                                        // Unregister the BroadcastReceiver after the installation
                                                                        context.unregisterReceiver(this);
                                                                } else {
                                                                    // Handle the case where the downloaded file URI is null
                                                                    Toast.makeText(context, "Failed to open downloaded file", Toast.LENGTH_SHORT).show();
                                                                }
                                                                }
                                                            }
                                                    };
                                                    // Register the BroadcastReceiver to listen for download completion
                                                    IntentFilter downloadFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
                                                    registerReceiver(broadcastReceiver, downloadFilter);


                                            } else {
                                                Toast.makeText(MainActivity.this, "Storage permission required", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                });
                                builder.create().show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
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
        if(databaseReference != null){
            databaseReference.removeEventListener(userNodeValueEventListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseAuth.getInstance().removeAuthStateListener(this);
        unregisterNetworkStateReceiver();
        if(databaseReference != null){
            databaseReference.removeEventListener(userNodeValueEventListener);
        }
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if(isUser()){
            //Do something
        }else{
            if(databaseReference != null){
                databaseReference.removeEventListener(userNodeValueEventListener);
            }
            startActivity(new Intent(this, AuthLogin.class));
            finish();
        }
    }
    private boolean isUser(){
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            return true;
        }
        return false;
    }

    public void handlesAccComplete(Boolean RESULT){
        if(!RESULT){
            Intent personalizeInfo = new Intent(this, PersonalInfo.class);
            personalizeInfo.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(personalizeInfo);
        }else{
            if(isUser()){
                user = FirebaseAuth.getInstance().getCurrentUser();
                assert user != null;
                nav_user_name.setText(user.getDisplayName());
                String imageUri = Objects.requireNonNull(user.getPhotoUrl()).toString();

                Picasso.get().load(imageUri).into(nav_user_image);
                Picasso.get().load(imageUri).into(nav_account);

                Menu menu = navigationView.getMenu();

                menu.findItem(R.id.nav_side_recipient_menu).setVisible(isRecipient);
                menu.findItem(R.id.nav_side_donor_menu).setVisible(isDonor);

                ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);
                viewPager2Main.setAdapter(viewPagerAdapter);
                TabLayout tabLayout = findViewById(R.id.tab_layout);
                new TabLayoutMediator(tabLayout, viewPager2Main,
                        (tab, position)->{
                            View customTabView = LayoutInflater.from(tabLayout.getContext()).inflate(R.layout.custom_tab_layout, null);
                            ImageButton tabTitle = customTabView.findViewById(R.id.ib_custom_tab);
                            switch (position){
                                case 0:{
                                    //tab.setText("Object" + (position + 1));
                                    tabTitle.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.dashbord_white, getTheme()));
                                    break;
                                }
                                case 1:{
                                    tabTitle.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.newspaper_white, getTheme()));
                                    break;
                                }
                                case 2:{
                                    tabTitle.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.message_white, getTheme()));
                                    break;
                                }
                                case 3:{
                                    tabTitle.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.notification, getTheme()));
                                    break;
                                }
                            }
                            tab.setCustomView(customTabView);
                        }).attach();
            }
        }
    }

    private void loadUserPhoto() {
        Glide.with(this)
                .asBitmap()
                .load(user.getPhotoUrl().toString())
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Drawable drawable = new BitmapDrawable(getResources(), resource);
                        nav_account.setImageDrawable(drawable);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Do nothing
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        nav_account.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.error, getTheme()));
                    }
                });
    }
    private void cacheUserInfo(){
        if(isUser()){
            databaseReference.child("Users").child(user.getUid()).get()
                    .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            Gson gson = new Gson();
                            UserModel userModel = task.getResult().getValue(UserModel.class);
                            getValues(userModel);
                            handlesAccComplete(userModel.getAccount_role().get("complete"));
                            String userData = gson.toJson(userModel);
                            editor.putString(user.getUid()+"_data", userData);
                            editor.apply();
                        }
                    }).addOnFailureListener(e->{
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    });
        }
    }
    private void getValues(UserModel userModel){
        if(userModel.getAccount_role().containsKey("Donor")){
            isDonor = true;
        } else if (userModel.getAccount_role().containsKey("Recipient")) {
            isRecipient = true;
        }

        isComplete = userModel.getAccount_role().get("complete");

        if(userModel.getAccount_role().containsKey("Individual")){
            isIndividual = true;
        } else if (userModel.getAccount_role().containsKey("Organisation")) {
            isOrganisation = true;
        }
    }


    private void viewProfile(UserModel userModel){
        String userData = gson.toJson(userModel);
        Intent profileIntent = new Intent(this, Profile.class);
        profileIntent.putExtra("userData", userData);
        profileIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(profileIntent);
    }
    private void addNewPost(){
        Intent addPost = new Intent(this, ActivityDonationInfo.class);
        addPost.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(addPost);
    }

    @Override
    public void onNetworkConnected() {
        if(isUser() && isActivityCreated){
            sharedPreferences = getSharedPreferences(user.getUid()+"_pref", MODE_PRIVATE);

            String userData = sharedPreferences.getString(user.getUid()+"_data", null);
                    userNodeValueEventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UserModel userModel = snapshot.getValue(UserModel.class);
                            String retrievedData = gson.toJson(userModel);
                            if(userData != null){
                                int retrievedHashCode = retrievedData.hashCode();
                                int cachedHashCode = userData.hashCode();

                                if(retrievedHashCode != cachedHashCode){
                                    cacheUserInfo();
                                }else {
                                    getValues(userModel);
                                    handlesAccComplete(isComplete);
                                }
                            }else {
                                cacheUserInfo();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    };
            databaseReference.child("Users").child(user.getUid()).addValueEventListener(userNodeValueEventListener);
            try {
                PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                String versionName = packageInfo.versionName;
                int versionCode = packageInfo.versionCode;
                System.out.println(versionName);
                checkUpdates(versionName);

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }


        }
    }
    @Override
    public void onNetworkDisconnected() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert");
        builder.setMessage("Device not connected");
        builder.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.warning, getTheme()));
        builder.setPositiveButton("Ok", (dialog, which)->{
            dialog.dismiss();
        });
        builder.create().show();
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
    private void loadUserProfile(){
        AlertDialog alertDialog = Progress.createAlertDialog(this, "Loading...");
        alertDialog.show();
        databaseReference.child("Users/"+ user.getUid())
                .get().addOnCompleteListener(task->{
                    if(task.isSuccessful()){
                        alertDialog.dismiss();
                        UserModel userModel = task.getResult().getValue(UserModel.class);
                        viewProfile(userModel);
                    }
                }).addOnFailureListener(e->{
                    alertDialog.dismiss();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:{
                return new Dashboard();
            }
            case 1:{
                return new PostFragment();
            }
            case 2:{
                return new MessagesFragment();
            }
            case 3:{
                return new NotificationsFragment();
            }
        }

        return null;
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}