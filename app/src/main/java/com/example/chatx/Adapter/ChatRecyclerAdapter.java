package com.example.chatx.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatx.Model.DataModels.ChatMessage;
import com.example.chatx.R;
import com.example.chatx.Utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class ChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatMessage, ChatRecyclerAdapter.ChatRecyclerViewHolder> {

    private Context context;

    public ChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatMessage> options, Context context) {
        super(options);
        this.context = context;
    }

    @NonNull
    @Override
    public ChatRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_layout,parent,false);
        return new ChatRecyclerViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatRecyclerViewHolder holder, int position, @NonNull ChatMessage model) {

        Log.d("lastMessage", model.getMessage());

        if(model.getSenderId().equals(FirebaseUtil.getCurrentUserId())){
            holder.leftLayout.setVisibility(View.GONE);
            holder.rightLayout.setVisibility(View.VISIBLE);
            holder.rightMsg.setText(model.getMessage());
        }
        else{
            holder.rightLayout.setVisibility(View.GONE);
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.leftMsg.setText(model.getMessage());
        }
    }

    public class ChatRecyclerViewHolder extends RecyclerView.ViewHolder{

        TextView leftMsg, rightMsg;
        LinearLayout leftLayout, rightLayout;

        public ChatRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            leftLayout = itemView.findViewById(R.id.leftTextLayout);
            rightLayout = itemView.findViewById(R.id.rightTextLayout);
            leftMsg = itemView.findViewById(R.id.leftText);
            rightMsg = itemView.findViewById(R.id.rightText);
        }
    }

}
