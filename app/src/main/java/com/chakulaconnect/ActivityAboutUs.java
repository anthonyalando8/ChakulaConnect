package com.chakulaconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.AlignContent;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.JustifyContent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityAboutUs extends AppCompatActivity {
    RelativeLayout rlAboutSecOne;
    RecyclerView rvTeam;

    DatabaseReference databaseReference;
    ArrayList<TeamModel> teamModels;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        rlAboutSecOne = findViewById(R.id.rlAboutSecOne);
        rvTeam = findViewById(R.id.rvTeam);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("About us");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        CustomFlexBox customFlexBox = new CustomFlexBox(this);

        customFlexBox.setFlexWrap(FlexWrap.WRAP);
        customFlexBox.setFlexDirection(FlexDirection.ROW);
        customFlexBox.setAlignItems(AlignItems.STRETCH);
        customFlexBox.setAlignContent(AlignContent.STRETCH);
        customFlexBox.setJustifyContent(JustifyContent.FLEX_START);

        databaseReference.child("About").get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()){
                            AboutSecOneModel aboutSecOneModel;
                            ArrayList<AboutSecOneModel> aboutSecOneModels = new ArrayList<>();
                            for (DataSnapshot snapshot : task.getResult().getChildren()){
                                if (snapshot.exists()){
                                    aboutSecOneModel = snapshot.getValue(AboutSecOneModel.class);
                                    aboutSecOneModels.add(aboutSecOneModel);
                                }
                            }
                            customFlexBox.setData(aboutSecOneModels);
                            rlAboutSecOne.addView(customFlexBox);
                        }
                    }
                }).addOnFailureListener(e->{
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        teamModels = new ArrayList<>();
        TeamAdapter teamAdapter = new TeamAdapter(this, teamModels);
        LinearLayoutManager layoutActivityManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvTeam.setLayoutManager(layoutActivityManager);
        rvTeam.setAdapter(teamAdapter);
        databaseReference.child("Team")
                .get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if(task.isSuccessful()){
                            for (DataSnapshot snapshot : task.getResult().getChildren()){
                                if(snapshot.exists()){
                                    TeamModel teamModel = snapshot.getValue(TeamModel.class);
                                    teamModel.setName(snapshot.child("name").getValue(String.class));
                                    teamModel.setEmail(snapshot.child("email").getValue(String.class));
                                    teamModel.setImageUri(snapshot.child("imageUri").getValue(String.class));
                                    teamModels.add(teamModel);
                                }
                            }
                            teamAdapter.notifyDataSetChanged();
                        }
                    }
                }).addOnFailureListener(e->{
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}

class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.AdapterHolder>{
    Context context;
    ArrayList<TeamModel> teamModels;
    public  TeamAdapter(Context context, ArrayList<TeamModel> teamModels){
        this.context = context;
        this.teamModels = teamModels;
    }
    @NonNull
    @Override
    public AdapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.team_single_custom, parent, false);
        return new AdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterHolder holder, int position) {
        TeamModel teamModel = teamModels.get(position);
        holder.txtTeamName.setText(teamModel.getName());
        holder.txtTeamDesc.setText(teamModel.getDescription());
        holder.txtTeamRole.setText(teamModel.getRole());
        Picasso.get().load(teamModel.getImageUri()).into(holder.ivTeamImage);
    }

    @Override
    public int getItemCount() {
        return teamModels.size();
    }

    class AdapterHolder extends RecyclerView.ViewHolder{
        TextView txtTeamName, txtTeamRole, txtTeamDesc;
        ImageView ivTeamImage;

        public AdapterHolder(@NonNull View itemView) {
            super(itemView);
            txtTeamName = itemView.findViewById(R.id.lblTeamName);
            txtTeamRole = itemView.findViewById(R.id.lblTeamRole);
            txtTeamDesc = itemView.findViewById(R.id.lblTeamDesc);
            ivTeamImage = itemView.findViewById(R.id.ivTeamImage);
        }
    }
}