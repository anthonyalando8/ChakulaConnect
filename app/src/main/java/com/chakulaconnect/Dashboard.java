package com.chakulaconnect;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Dashboard extends Fragment {
    private ArrayList<DashOneModel> dashOneModels;
    private ArrayList<UserActivityModel> userActivityModels;
    private  DashUserActivityAdapter dashUserActivityAdapter;
    private RecyclerView rvActivity;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private FirebaseUser user;
    TextView txt_btn_see_all_activity;
    SharedPreferences sharedPreferences;
    private ViewPager2 vp_dashOne;
    Gson gson;
    String userData;
    private int currentPage = 0;
    private Handler handler = new Handler();
    private Runnable runnable;
    private final long DELAY_TIME = 3000;
    public Dashboard() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        gson = new Gson();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        if(isUser()){
            sharedPreferences = getActivity().getSharedPreferences(user.getUid()+"_pref", Context.MODE_PRIVATE);
            userData = sharedPreferences.getString(user.getUid()+"_data", null);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvActivity = getView().findViewById(R.id.rv_activity);
        txt_btn_see_all_activity = getView().findViewById(R.id.txt_btn_see_all_activity);
        vp_dashOne = getView().findViewById(R.id.vp_dash_one);


        LinearLayoutManager layoutActivityManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        dashOneModels = new ArrayList<>();
        dashOneModels.add(new DashOneModel("https://ik.imagekit.io/anthonyalando/Chakula%20Connect/support-us.jpeg?updatedAt=1693318075336", "Asking for support", "Chakula connect team is asking for your support", "https://ik.imagekit.io/anthonyalando/Chakula%20Connect/contribute_.jpg?updatedAt=1692550519717"));
        dashOneModels.add(new DashOneModel("https://ik.imagekit.io/anthonyalando/Chakula%20Connect/donate-.jpeg?updatedAt=1693318076184", "Heartfelt Journeys", "Empowering Lives, One Story at a Time.", "https://ik.imagekit.io/anthonyalando/Chakula%20Connect/power_.jpg?updatedAt=1692550519104"));
        dashOneModels.add(new DashOneModel("https://ik.imagekit.io/anthonyalando/Chakula%20Connect/t-you.jpeg?updatedAt=1693318076098", "Gratitude Circle", "Your Kindness Creates Lasting Impact. Thank You!", "https://ik.imagekit.io/anthonyalando/Chakula%20Connect/thank_.jpg?updatedAt=1692550519613"));
        dashOneModels.add(new DashOneModel("https://ik.imagekit.io/anthonyalando/Chakula%20Connect/c-project.png?updatedAt=1693318076363", "Current projects", "Nutrition Matters. Feeding Families. Food Security Program. Learn More.", "https://ik.imagekit.io/anthonyalando/Chakula%20Connect/project_.jpg?updatedAt=1692550981901"));
        dashOneModels.add(new DashOneModel("https://ik.imagekit.io/anthonyalando/Chakula%20Connect/l-more.jpeg?updatedAt=1693318076290", "Learn more", "Explore Features, Maximize Benefits. Discover Today", "https://ik.imagekit.io/anthonyalando/Chakula%20Connect/explore_.jpg?updatedAt=1692550519402"));

        DashOneAdapter dashOneAdapter = new DashOneAdapter(getContext(), dashOneModels);
        vp_dashOne.setAdapter(dashOneAdapter);
        startAutoScroll();
        rvActivity.setLayoutManager(layoutActivityManager);

        if(isUser()){
            retrieveUserActivity();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    private void startAutoScroll() {
        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }

        runnable = new Runnable() {
            @Override
            public void run() {
                if (currentPage == dashOneModels.size()) {
                    currentPage = 0;
                }
                vp_dashOne.setCurrentItem(currentPage++, true);
                handler.postDelayed(this, DELAY_TIME);
            }
        };

        handler.postDelayed(runnable, DELAY_TIME);
    }

    public void retrieveUserActivity(){
        if(userData != null){
            UserModel userModel = gson.fromJson(userData, UserModel.class);
            HashMap<String, UserActivityModel> activityHashMap = userModel.getActivity();
            userActivityModels = new ArrayList<>();
            if(activityHashMap != null && !activityHashMap.isEmpty()){
                //userActivityModels = new ArrayList<>(userModel.getActivity().values());
                // Step 1: Transfer entries to a list and sort them
                List<Map.Entry<String, UserActivityModel>> entryList = new ArrayList<>(activityHashMap.entrySet());

                Collections.sort(entryList, new Comparator<Map.Entry<String, UserActivityModel>>() {
                    @Override
                    public int compare(Map.Entry<String, UserActivityModel> entry1, Map.Entry<String, UserActivityModel> entry2) {
                        // Convert the keys to long and compare them
                        long key1 = Long.parseLong(entry1.getKey());
                        long key2 = Long.parseLong(entry2.getKey());
                        return Long.compare(key2, key1);
                    }
                });

                // Step 4: Iterate through the sorted entry list and add UserActivityModels
                int count = 0; // Counter for tracking the number of added entries
                for (Map.Entry<String, UserActivityModel> entry : entryList) {
                    userActivityModels.add(entry.getValue());

                    count++;
                    if (count >= 5) {
                        break; // Stop adding after the first 5 entries
                    }
                }
            }else{
                userActivityModels = new ArrayList<>();
                userActivityModels.add(new UserActivityModel("No recent activities", "All activity deleted", "", "no_activity", null));
            }
            dashUserActivityAdapter = new DashUserActivityAdapter(userActivityModels, getContext(), txt_btn_see_all_activity);
            rvActivity.setAdapter(dashUserActivityAdapter);
            dashUserActivityAdapter.notifyDataSetChanged();
        }
    }
    public  boolean isUser(){
        if(user != null){
            return true;
        }
        return false;
    }

}

class DashOneAdapter extends RecyclerView.Adapter<DashOneAdapter.DashOneHolder> {
    Context context;
    ArrayList<DashOneModel> dashOneModels;

    public DashOneAdapter(Context context, ArrayList<DashOneModel> dashOneModels) {
        this.context = context;
        this.dashOneModels = dashOneModels;
    }

    @NonNull
    @Override
    public DashOneHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.dashboard_slide_top, parent, false);
        return new DashOneHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DashOneHolder holder, int position) {
        DashOneModel dashOneModel = dashOneModels.get(position);
        String title = dashOneModel.getTitle();
        String body = dashOneModel.getContent();
        String background = dashOneModel.getBackground();
        String imageUri = dashOneModel.getImageUri();

        holder.txt_dash_one_title.setText(title);
        holder.txt_dash_one_body.setText(body);
        try{
            Picasso.get().load(imageUri).into(holder.civ_dash_one_image);
        }catch (Exception e){
            holder.civ_dash_one_image.setImageDrawable(ResourcesCompat.getDrawable(Resources.getSystem(), R.drawable.logo_transparent, context.getTheme()));
        }
        RequestOptions requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL); // Cache the image to avoid reloading
        Glide.with(context)
                .load(background)
                .apply(RequestOptions
                        .bitmapTransform(new MultiTransformation<>(
                                new CenterCrop(),
                                new RoundedCorners(8)))
                        .override(320, 150) // Resize to 320x150 dimensions
                        .format(DecodeFormat.PREFER_RGB_565) // Set image format to RGB_565 for smaller size
                )
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        int darkColor = Color.parseColor("#50000000"); // Semi-transparent black
                        PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(darkColor, PorterDuff.Mode.SRC_ATOP);
                        resource.setColorFilter(colorFilter);
                        holder.cl_dash_one_layout.setBackground(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        //
                    }
                });



    }

    @Override
    public int getItemCount() {
        return dashOneModels.size();
    }

    public class DashOneHolder extends RecyclerView.ViewHolder{
        CircleImageView civ_dash_one_image;
        TextView txt_dash_one_title, txt_dash_one_body;
        ConstraintLayout cl_dash_one_layout;
        public DashOneHolder(@NonNull View itemView) {
            super(itemView);
            civ_dash_one_image = itemView.findViewById(R.id.civ_dash_one_image);
            txt_dash_one_title = itemView.findViewById(R.id.txt_dash_one_title);
            txt_dash_one_body = itemView.findViewById(R.id.txt_dash_one_body);
            cl_dash_one_layout = itemView.findViewById(R.id.cl_dash);
        }
    }
}

class DashOneModel{
    public DashOneModel() {
        //Empty constructor
    }

    public DashOneModel(String imageUri, String title, String content, String background) {
        this.imageUri = imageUri;
        this.title = title;
        this.content = content;
        this.background = background;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    String imageUri, title, content, background;
}

class DashUserActivityAdapter extends RecyclerView.Adapter<DashUserActivityAdapter.ActivityHolder> {
    private ArrayList<UserActivityModel> userActivityModels;
    private Context context;
    TextView txt_btn_see_all_activity;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private DatabaseReference reference;

    public DashUserActivityAdapter(ArrayList<UserActivityModel> userActivityModels, Context context, TextView txt_btn_see_all_activity) {
        this.userActivityModels = userActivityModels;
        this.context = context;
        this.txt_btn_see_all_activity = txt_btn_see_all_activity;
    }

    @NonNull
    @Override
    public ActivityHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        View view = LayoutInflater.from(context).inflate(R.layout.activity_dashboard_single_layout, parent, false);
        return new ActivityHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityHolder holder, int position) {
        UserActivityModel userActivityModel = userActivityModels.get(position);
        if(userActivityModel.getActivityId().equals("no_activity")){
            holder.civ_activityImage.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.info, context.getTheme()));
            holder.txt_activityTitle.setText(userActivityModel.getActivityTitle());
            holder.ib_remove_activity.setVisibility(View.GONE);
            txt_btn_see_all_activity.setVisibility(View.GONE);
            return;
        }else{
            holder.txt_activityTitle.setText(userActivityModel.getActivityTitle());
            Long activityDate = userActivityModel.getActivityDate();
            Instant instant = Instant.ofEpochMilli(activityDate);
            LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            holder.txt_activityDate.setText(localDateTime.format(formatter));
            holder.txtActivityDescription.setText(userActivityModel.getActivityDescription());
            holder.cl_btn_view_activity.setOnClickListener(v->{
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(userActivityModel.getActivityTitle());
                builder.setMessage(userActivityModel.getActivityDescription());
                builder.setPositiveButton("Ok", (dialog, which)->{
                    dialog.dismiss();
                });
                builder.setIcon(ResourcesCompat.getDrawable(context.getResources(), R.drawable.activity, context.getTheme()));
                builder.create().show();
            });
            if(userActivityModel.getActivityFlag().equals(user.getUid())){
                holder.civ_activityImage.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.account_manager, context.getTheme()));
            }else{
                reference.child("Users").child(userActivityModel.getActivityFlag()).child("accountDetails").child("imageUri")
                        .get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                String imageUri = task.getResult().toString();
                                Picasso.get()
                                        .load(imageUri)
                                        .into(holder.civ_activityImage);
                            }
                        }).addOnFailureListener(e->{
                            holder.civ_activityImage.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.activity, context.getTheme()));
                        });
            }
            holder.ib_remove_activity.setOnClickListener(v->{
                Toast.makeText(context, "Removing", Toast.LENGTH_SHORT).show();
                DatabaseReference activityRef = FirebaseDatabase.getInstance().getReference()
                        .child("Users/"+FirebaseAuth.getInstance().getCurrentUser()
                                .getUid()+"/activity/"+userActivityModel.getActivityId());

                activityRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

    }

    @Override
    public int getItemCount() {
        return userActivityModels.size();
    }

    class ActivityHolder extends RecyclerView.ViewHolder{
        private CircleImageView civ_activityImage;
        private TextView txt_activityTitle, txt_activityDate, txtActivityDescription;
        private ImageButton ib_remove_activity;
        ConstraintLayout cl_btn_view_activity;
        public ActivityHolder(@NonNull View itemView) {
            super(itemView);
            civ_activityImage = itemView.findViewById(R.id.civ_activity_image);
            txt_activityTitle = itemView.findViewById(R.id.txt_activity_name);
            txt_activityDate = itemView.findViewById(R.id.txt_activity_date);
            ib_remove_activity = itemView.findViewById(R.id.ib_remove_activity);
            txtActivityDescription = itemView.findViewById(R.id.txt_activity_description);
            cl_btn_view_activity = itemView.findViewById(R.id.cl_btn_viewActivity);
        }
    }
}