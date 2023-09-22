package com.chakulaconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    String userInfo;
    Gson gson;
    UserModel userModel;
    EditText etMessage;
    ImageButton ibSend, ibAttach;
    String strMessage;
    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;
    DatabaseReference databaseReference;
    DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("Messages");
    DatabaseReference uniqueKey;
    StorageReference storageReference;
    ChatModel chatModel;
    RecyclerView rvChatText;
    ArrayList<ChatModel> chatModels;

    ChatAdapter chatAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        gson = new Gson();
        chatModels = new ArrayList<>();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        userInfo = getIntent().getStringExtra("userInfo");

        etMessage = findViewById(R.id.etMessage);
        ibSend = findViewById(R.id.ibSend);
        ibAttach = findViewById(R.id.ibAttach);

        rvChatText = findViewById(R.id.rvChatText);

        if(userInfo != null){
            userModel = gson.fromJson(userInfo, UserModel.class);
            if(getSupportActionBar() != null){
                getSupportActionBar().setTitle(userModel.getAccountDetails().get("userName"));
                getSupportActionBar().setSubtitle(userModel.getAccountDetails().get("email"));
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        ibSend.setOnClickListener(v->{
            strMessage = etMessage.getText().toString().trim();
            if(validateMessage(strMessage) && (userInfo) != null || userInfo.isEmpty() && isUser()){
                uniqueKey = messagesRef.push();
                chatModel = new ChatModel(currentUser.getUid(), userModel.getAccountDetails().get("userId"),
                        String.valueOf(System.currentTimeMillis()), "Send", strMessage, uniqueKey.toString());
                sendMessage(chatModel);
            }else {
                Toast.makeText(this, "Application error", Toast.LENGTH_SHORT).show();
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        chatAdapter = new ChatAdapter(chatModels, this, userModel, currentUser);
        rvChatText.setLayoutManager(linearLayoutManager);
        rvChatText.setAdapter(chatAdapter);
        retrieveChats();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
    private boolean validateMessage(String message){
        if(message.isEmpty()){
            return false;
        }
        return true;
    }
    private void sendMessage(ChatModel chatModel){
        uniqueKey.setValue(chatModel).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                etMessage.setText("");
            }
        }).addOnFailureListener(e->{
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void retrieveChats(){
        databaseReference.child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    chatModels.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        ChatModel singleChat = dataSnapshot.getValue(ChatModel.class);
                        if((singleChat.getSender().equals(currentUser.getUid()) &&
                                singleChat.getReceiver().equals(userModel.getAccountDetails().get("userId")) )
                                || (singleChat.getReceiver().equals(currentUser.getUid()) &&
                                singleChat.getSender().equals(userModel.getAccountDetails().get("userId")))){
                            chatModels.add(singleChat);
                        }
                    }
                    chatAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private boolean isUser(){
        if(currentUser == null){
            return false;
        }
        return true;
    }
}
class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatHolder>{
    ArrayList<ChatModel> chatModels;
    Context context;
    UserModel userModel;
    FirebaseUser user;

    public ChatAdapter(ArrayList<ChatModel> chatModels, Context context, UserModel userModel, FirebaseUser user) {
        this.chatModels = chatModels;
        this.context = context;
        this.userModel = userModel;
        this.user = user;
    }

    @NonNull
    @Override
    public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_received_msg, parent, false);
        return new ChatHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatHolder holder, int position) {
        ChatModel chatModel = chatModels.get(position);
        holder.txtMessage.setText(chatModel.getMessage());
        Long msgTime = Long.parseLong(chatModel.getTime());

        Instant instant = Instant.ofEpochMilli(msgTime);
        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        holder.txtTime.setText(localDateTime.format(formatter));

        View clMain = holder.clMain;
        ViewGroup.MarginLayoutParams layoutParamsMain = (ViewGroup.MarginLayoutParams) clMain.getLayoutParams();
        if (chatModel.getReceiver().equals(user.getUid())){
            Picasso.get().load(userModel.getAccountDetails().get("imageUri")).into(holder.civUserImageChat);
            holder.civUserImageChat.setVisibility(View.VISIBLE);
            layoutParamsMain.setMargins(2,8,50,8);
            holder.clChangeBg.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.receive_msg_bg, context.getTheme()));
        }else {
            holder.civUserImageChat.setVisibility(View.GONE);
            layoutParamsMain.setMargins(80,8,2,8);
            holder.clChangeBg.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.send_msg_bg, context.getTheme()));
        }
        holder.clMain.setLayoutParams(layoutParamsMain);
    }

    @Override
    public int getItemCount() {
        return chatModels.size();
    }

    class ChatHolder extends RecyclerView.ViewHolder{
        CircleImageView civUserImageChat;
        TextView txtMessage, txtTime;
        ConstraintLayout clMain, clChangeBg;


        public ChatHolder(@NonNull View itemView) {
            super(itemView);
            civUserImageChat = itemView.findViewById(R.id.civMessageUserImage);
            txtMessage = itemView.findViewById(R.id.txtMessageSingle);
            txtTime = itemView.findViewById(R.id.txtMessageTime);

            clMain = itemView.findViewById(R.id.clMainMsg);
            clChangeBg = itemView.findViewById(R.id.clChangeBg);
        }
    }
}