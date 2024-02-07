package com.example.chatx.View.Main.Chat;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatx.Adapter.ChatRecyclerAdapter;
import com.example.chatx.Model.DataModels.ChatMessage;
import com.example.chatx.Model.DataModels.ChatRoom;
import com.example.chatx.Model.DataModels.User;
import com.example.chatx.R;
import com.example.chatx.Utils.Callbacks.OnChatRoomDataRetrievedListener;
import com.example.chatx.Utils.Callbacks.OnCompleteCallback;
import com.example.chatx.Utils.FirebaseUtil;
import com.example.chatx.ViewModel.ChatViewModel;
import com.example.chatx.ViewModel.ReadWriteViewModel;
import com.example.chatx.databinding.ActivityChatBinding;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding xml;
    private User otherUser;
    private ReadWriteViewModel readWriteViewModel;
    private ChatViewModel chatViewModel;

    private ChatRecyclerAdapter adapter;

    private ChatRoom currentChatRoom;
    private String chatRoomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        xml = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(xml.getRoot());

        //getting intent (user obj)
        otherUser = (User) getIntent().getSerializableExtra("User");
        chatRoomId = FirebaseUtil.getChatRoomId(otherUser.getUserId());
        chatViewModel = ChatViewModel.getInstance(getApplication());

        setUserData(otherUser);
        getChatRoomData(otherUser.getUserId());
        callQuery();

        xml.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        xml.chatLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!xml.chatEditText.getText().toString().isBlank()){
                    sendMessage(xml.chatEditText.getText().toString().trim());
                    xml.chatEditText.setText("");
                }
            }
        });

    }

    private void callQuery(){
        chatViewModel.getMessageFromChat(chatRoomId).observe(this, new Observer<Query>() {
            @Override
            public void onChanged(Query query) {
                setUpAdapter(query);
            }
        });

    }

    private void setUpAdapter(Query query){

        FirestoreRecyclerOptions<ChatMessage> options = new FirestoreRecyclerOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage.class).build();

        adapter = new ChatRecyclerAdapter(options,getApplicationContext());
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        xml.recyclerView.setLayoutManager(manager);
        xml.recyclerView.setAdapter(adapter);
        adapter.startListening();

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                scrollToLastMessage();
            }
        });


        xml.recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                // Remove the listener to avoid continuously scrolling
                xml.recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);

                // Scroll to the last item
                scrollToLastMessage();

                // Return true to continue with the current drawing pass
                return true;
            }
        });


        xml.recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // Check if the layout change is due to the keyboard being shown
                if (bottom < oldBottom) {
                    // Scroll to the last item
                    scrollToLastMessage();
                }
            }
        });

    }

    private void scrollToLastMessage(){
        if (adapter.getItemCount() > 0) {
            xml.recyclerView.scrollToPosition(0);
        }
    }

    private void setUserData(User user){
        xml.name.setText(user.getName());
        setPicture(xml.profilePic, Uri.parse(user.getPfp()));

    }

    private void sendMessage(String msg){
        chatViewModel.sendMessageToChat(currentChatRoom, msg, otherUser, new OnCompleteCallback() {
            @Override
            public void onComplete() {
                Log.d("dum","semd");
            }

            @Override
            public void onError(String ex) {
                Toast.makeText(ChatActivity.this, "failed: "+ex, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotification(String msg){

    }

    public void getChatRoomData(String otherUserId){
       chatViewModel.getChatRoomData(otherUserId, new OnChatRoomDataRetrievedListener() {
           @Override
           public void onChatRoomDataRetrieved(ChatRoom chatRoom) {

               currentChatRoom = chatRoom;

               if(currentChatRoom != null){
                   Log.d("currentChatRoom", "is not null");
               }
               else{
                   Log.d("currentChatRoom", "is null");
               }
           }
       });


    }

    private void setPicture(ImageView imageView, Uri imageUri){

        showPfpProgressBar();

        Picasso.get()
                .load(imageUri)
                .error(R.drawable.default_pfp_img)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        hidePfpProgressBar();
                    }

                    @Override
                    public void onError(Exception e) {
                        hidePfpProgressBar();
                    }
                });
    }

    private void showPfpProgressBar(){
        xml.profilePic.setVisibility(View.INVISIBLE);
        xml.pfpProgressBar.setVisibility(View.VISIBLE);
    }

    private void hidePfpProgressBar(){
        xml.profilePic.setVisibility(View.VISIBLE);
        xml.pfpProgressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(adapter!=null){
            adapter.startListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(adapter!=null){
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(adapter!=null){
            adapter.stopListening();
        }
    }
}