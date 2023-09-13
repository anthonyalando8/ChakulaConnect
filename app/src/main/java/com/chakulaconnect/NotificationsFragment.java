package com.chakulaconnect;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationsFragment extends Fragment {
    ArrayList<NotificationModel> notificationModels = new ArrayList<>();
    DatabaseReference notificationRef;
    FirebaseAuth firebaseAuth;
    SharedPreferences sharedPreferences;
    FirebaseUser user;
    UserModel userModel;
    Boolean isDonor = false, isRecipient = false, isOrg = false, isIndividual = false;
    Gson gson;
    String userData;
    public NotificationsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationRef = FirebaseDatabase.getInstance().getReference("Notifications");
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        gson = new Gson();
        if(isUser()){
            sharedPreferences = getActivity().getSharedPreferences(user.getUid()+"_pref", Context.MODE_PRIVATE);
            userData = sharedPreferences.getString(user.getUid()+"_data", null);
            if(userData != null){
                userModel = gson.fromJson(userData, UserModel.class);

                if(userModel.getAccount_role().containsKey("Donor")){
                    isDonor = true;
                } else if (userModel.getAccount_role().containsKey("Recipient")) {
                 isRecipient = true;
                }
                if(userModel.getAccount_role().containsKey("Individual")){
                    isIndividual = true;
                } else if (userModel.getAccount_role().containsKey("Organisation")) {
                    isOrg = true;
                }
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        RecyclerView rvNotifications = view.findViewById(R.id.rvNotifications);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvNotifications.setLayoutManager(linearLayoutManager);
        NotificationAdapter notificationAdapter = new NotificationAdapter(getContext(), notificationModels);
        rvNotifications.setAdapter(notificationAdapter);

        notificationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    NotificationModel notificationModel;
                    notificationModels.clear();
                    for (DataSnapshot snapshot1 : snapshot.getChildren()){
                        if (snapshot1.exists()){
                            String notifyRole = snapshot1.child("flags").child("Role").getValue(String.class);
                            String notifyEntity = snapshot1.child("flags").child("Entity").getValue(String.class);
                            if (isDonor && notifyRole.equals("Donor") && (((isIndividual && notifyEntity.equals("Individual")) ||
                                    (isOrg && notifyEntity.equals("Organisation")) ) || notifyEntity.equals("All")) ){
                                notificationModel = snapshot1.getValue(NotificationModel.class);
                                notificationModels.add(notificationModel);
                            } else if (isRecipient && notifyRole.equals("Recipient") && (((isIndividual && notifyEntity.equals("Individual")) ||
                                    (isOrg && notifyEntity.equals("Organisation")) ) || notifyEntity.equals("All")) ) {
                                notificationModel = snapshot1.getValue(NotificationModel.class);
                                notificationModels.add(notificationModel);
                            }
                            notificationAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return view;
    }
    private boolean isUser(){
        if(user != null){
            return true;
        }
        return false;
    }
}
class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationHolder>{
    Context context;
    ArrayList<NotificationModel>  notificationModels;
    public NotificationAdapter(Context context, ArrayList<NotificationModel> notificationModels){
        this.context = context;
        this.notificationModels = notificationModels;
    }
    @NonNull
    @Override
    public NotificationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_single_notification, parent, false);
        return new NotificationHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationHolder holder, int position) {
        NotificationModel notificationModel = notificationModels.get(position);
        holder.lblNotificationTitle.setText(notificationModel.getNotifyBody().concat("\n"+notificationModel.notifyDescription));
        holder.lblNotificationTime.setText(TimeCalculator.calculateTime(Long.parseLong(notificationModel.notifyDate)));
        Picasso.get().load(notificationModel.getImageUri()).into(holder.civNotificationIcon);
        PopupMenu popupMenu = new PopupMenu(context, holder.ibMoreVert);
        holder.ibMoreVert.setOnClickListener(v->{

            popupMenu.getMenuInflater().inflate(R.menu.notification_popup, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.not_nav_claim){
                    //Do something
                }
                if (menuItem.getItemId() == R.id.not_nav_donate){
                    //Do something
                }
                if (menuItem.getItemId() == R.id.not_nav_mark_seen){
                    //Do something
                }
                if (menuItem.getItemId() == R.id.not_nav_view_full){
                    //Do something
                }
                return false;
            });
        });
    }

    @Override
    public int getItemCount() {
        return notificationModels.size();
    }

    class NotificationHolder extends RecyclerView.ViewHolder{
            TextView lblNotificationTitle, lblNotificationTime;
            CircleImageView civNotificationIcon;
            ImageButton ibMoreVert;

        public NotificationHolder(@NonNull View itemView) {
            super(itemView);

            lblNotificationTitle = itemView.findViewById(R.id.txtNotificationLabel);
            lblNotificationTime = itemView.findViewById(R.id.txtNotificationTime);
            civNotificationIcon = itemView.findViewById(R.id.civNotificationIcon);
            ibMoreVert = itemView.findViewById(R.id.ibMoreVert);
        }
    }
}
