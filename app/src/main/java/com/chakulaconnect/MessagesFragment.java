package com.chakulaconnect;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesFragment extends Fragment implements NetworkStateReceiver.NetworkStateListener {
    private FloatingActionButton fabAddNewChart;
    private RecyclerView rvChatList;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    ChatModel chatModel;
    Set<ChatModel> chatList;
    ArrayList<ChatModel> allChats;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Gson gson;
    String storedChatList;
    ChatListAdapter chatListAdapter;
    ArrayList<UserModel> userModels = new ArrayList<>();
    HashMap<String, ArrayList<ChatModel>> userMessages = new HashMap<>();
    public MessagesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        chatList = new TreeSet<>(Comparator.comparingLong(chat->{
            try {
                return Long.parseLong(chat.getTime());
            }catch (Exception e){
                e.printStackTrace();
                return 0;
            }
        }));
        allChats = new ArrayList<>();
        gson = new Gson();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        if (isUser()){
            sharedPreferences = getContext().getSharedPreferences(user.getUid()+"_pref", Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
            storedChatList= sharedPreferences.getString("storedChatList", null);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fabAddNewChart = getView().findViewById(R.id.fabAddChart);
        rvChatList = getView().findViewById(R.id.rvChatList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        rvChatList.setLayoutManager(linearLayoutManager);
        chatListAdapter = new ChatListAdapter(getContext(), userModels, userMessages);
        rvChatList.setAdapter(chatListAdapter);

        loadChats();
        fabAddNewChart.setOnClickListener(v->{
            Intent addNewChart = new Intent(getContext(), UsersActivity.class);
            addNewChart.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(addNewChart);
        });
    }
    private void loadChats(){
        databaseReference.child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    chatList.clear();
                    allChats.clear();
                    userModels.clear();
                    userMessages.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        ChatModel singleChat = dataSnapshot.getValue(ChatModel.class);
                        if(singleChat.getSender().equals(user.getUid()) || singleChat.getReceiver().equals(user.getUid())){
                            allChats.add(singleChat);
                        }
                    }
                    if (!allChats.isEmpty() && allChats != null){
                        for (ChatModel chatModel1 : allChats){
                            if (!chatList.isEmpty()){
                                boolean chatModelExists = false;

                                for (ChatModel chatModel2 : chatList) {
                                    if ((chatModel2.getSender().equals(chatModel1.getSender()) && chatModel2.getReceiver().equals(chatModel1.getReceiver()))
                                            || (chatModel2.getSender().equals(chatModel1.getReceiver()) && chatModel2.getReceiver().equals(chatModel1.getSender()))) {
                                        // A matching chat model meeting the conditions exists in the list
                                        chatModelExists = true;
                                        break; // Exit the loop, no need to continue checking
                                    }
                                }

                                if (!chatModelExists) {
                                    // If chatModelExists is still false, it means the condition was not met in the loop
                                    // Add chatModel1 to the list
                                    chatList.add(chatModel1);
                                }
                            }else {
                                chatList.add(chatModel1);
                            }

                        }
                        String chats;
                        chats = gson.toJson(chatList);
                        if(storedChatList != null){
                            int savedHash = storedChatList.hashCode();
                            int retrieveHash = chats.hashCode();

                            if(savedHash != retrieveHash){
                                editor.putString("storedChatList", chats);
                                editor.apply();
                            }
                        }else {
                            editor.putString("storedChatList", chats);
                            editor.apply();
                        }
                        loadChatProfile(chatList);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void loadChatProfile(Set<ChatModel> chatModelsUser) {
        for(ChatModel chatModel1 : chatModelsUser){
            ArrayList<ChatModel> chatModelArrayList = new ArrayList<>();
            for (ChatModel chatModel2 : allChats){
                if ((chatModel2.getSender().equals(chatModel1.getSender()) && chatModel2.getReceiver().equals(chatModel1.getReceiver())) ||
                        (chatModel2.getSender().equals(chatModel1.getReceiver()) && chatModel2.getReceiver().equals(chatModel1.getSender()))){
                    chatModelArrayList.add(chatModel2);
                }
            }
            userMessages.put(chatModel1.getSender().equals(user.getUid()) ? chatModel1.getReceiver() : chatModel1.getSender(), chatModelArrayList);
            databaseReference.child("Users/"+(chatModel1.getSender().equals(user.getUid())
                    ? chatModel1.getReceiver() : chatModel1.getSender())).get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            UserModel userModel = task.getResult().getValue(UserModel.class);
                            userModels.add(userModel);
                            chatListAdapter.notifyDataSetChanged();
                        }
                        else {
                            Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e->{
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
        System.out.println(userMessages);
    }
    private boolean isUser(){
        if (user != null){
            return true;
        }
        return false;
    }

    @Override
    public void onNetworkConnected() {
        //loadChats();
    }

    @Override
    public void onNetworkDisconnected() {

    }
}
class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListHolder>{
    Context context;
    ArrayList<UserModel> chatModelsUser;
    ArrayList<UserModel> userModels;
    HashMap<String, ArrayList<ChatModel>> userMessage;

    public ChatListAdapter(Context context, ArrayList<UserModel> chatModelsUser, HashMap<String, ArrayList<ChatModel>> userMessage) {
        this.context = context;
        this.chatModelsUser = chatModelsUser;
        this.userMessage = userMessage;
    }

    @NonNull
    @Override
    public ChatListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_single_chart, parent, false);
        return new ChatListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListHolder holder, int position) {
        UserModel userModel = chatModelsUser.get(position);
        ArrayList<ChatModel> chatModelArrayList = new ArrayList<>(Objects.requireNonNull(userMessage.get(userModel.getAccountDetails().get("userId"))));
        holder.txtName.setText(userModel.getAccountDetails().get("userName"));
        Picasso.get().load(userModel.getAccountDetails().get("imageUri")).into(holder.circleImageView);
        holder.txtMessage.setText(chatModelArrayList.get(chatModelArrayList.size() - 1).getMessage());
        holder.clViewChatActivity.setOnClickListener(v->{
            Intent chatIntent = new Intent(context, ChatActivity.class);
            chatIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Gson gson = new Gson();
            String userInfo = gson.toJson(userModel);
            chatIntent.putExtra("userInfo", userInfo);
            context.startActivity(chatIntent);
        });
    }

    @Override
    public int getItemCount() {
        return chatModelsUser.size();
    }

    class ChatListHolder extends RecyclerView.ViewHolder{
        CircleImageView circleImageView;
        TextView txtName;
        TextView txtMessage;
        ConstraintLayout clViewChatActivity;
        public ChatListHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById(R.id.civUserChatImage);
            txtMessage = itemView.findViewById(R.id.txtUserChatLastMessage);
            txtName = itemView.findViewById(R.id.txtUserChatName);
            clViewChatActivity = itemView.findViewById(R.id.clViewActivityChat);
        }
    }
}