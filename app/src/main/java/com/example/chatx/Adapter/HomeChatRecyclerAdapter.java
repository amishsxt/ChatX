package com.example.chatx.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatx.Model.DataModels.ChatRoom;
import com.example.chatx.Model.DataModels.User;
import com.example.chatx.R;
import com.example.chatx.Utils.Callbacks.OnUserDataRetrievedListener;
import com.example.chatx.Utils.FirebaseUtil;
import com.example.chatx.View.Main.Chat.ChatActivity;
import com.example.chatx.ViewModel.ChatViewModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class HomeChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatRoom, HomeChatRecyclerAdapter.HomeChatRecyclerViewHolder> {

    private Context context;
    private ChatViewModel chatViewModel;

    public HomeChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatRoom> options, Context context, ChatViewModel chatViewModel) {
        super(options);
        this.context = context;
        this.chatViewModel = chatViewModel;
    }

    @NonNull
    @Override
    public HomeChatRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.home_chat_layout,parent,false);
        return new HomeChatRecyclerViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull HomeChatRecyclerViewHolder holder, int position, @NonNull ChatRoom model) {

        chatViewModel.getThisUserData(model.getUserIds()
                , new OnUserDataRetrievedListener() {
                    @Override
                    public void onUserDataRetrievedListener(User user) {

                        if(user.getUserId().equals(FirebaseUtil.getCurrentUserId())){
                            holder.name.setText(user.getName() + " (You)");
                            holder.lastMsg.setText("You: " + model.getLastMessage());
                        }
                        else {
                            holder.name.setText(user.getName());
                            holder.lastMsg.setText(model.getLastMessage());
                        }

                        holder.lastTimestamp.setText(FirebaseUtil.timestampToString(
                                model.getLastMsgTimeStamp()));

                        setPicture(holder.pfp, Uri.parse(user.getPfp()), holder);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(context, ChatActivity.class);
                                intent.putExtra("User",user);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            }
                        });
                    }
                });

    }

    public class HomeChatRecyclerViewHolder extends RecyclerView.ViewHolder{

        TextView name, lastMsg, lastTimestamp;
        ImageView pfp;
        ProgressBar pfpProgressBar;

        public HomeChatRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            lastMsg = itemView.findViewById(R.id.lastMsg);
            lastTimestamp = itemView.findViewById(R.id.lastMsgTimestamp);
            pfp = itemView.findViewById(R.id.profilePic);
            pfpProgressBar = itemView.findViewById(R.id.pfpProgressBar);
        }
    }

    private void setPicture(ImageView imageView, Uri imageUri, HomeChatRecyclerViewHolder holder){

        holder.pfp.setVisibility(View.INVISIBLE);
        holder.pfpProgressBar.setVisibility(View.VISIBLE);

        Picasso.get()
                .load(imageUri)
                .error(R.drawable.default_pfp_img)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.pfp.setVisibility(View.VISIBLE);
                        holder.pfpProgressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        holder.pfp.setVisibility(View.VISIBLE);
                        holder.pfpProgressBar.setVisibility(View.GONE);
                    }
                });
    }

}
