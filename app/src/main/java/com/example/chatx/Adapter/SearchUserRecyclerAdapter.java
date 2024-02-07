package com.example.chatx.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatx.Model.DataModels.User;
import com.example.chatx.R;
import com.example.chatx.Utils.FirebaseUtil;
import com.example.chatx.View.Main.Chat.ChatActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class SearchUserRecyclerAdapter extends FirestoreRecyclerAdapter<User, SearchUserRecyclerAdapter.SearchUserRecyclerViewHolder> {

    private Context context;

    public SearchUserRecyclerAdapter(@NonNull FirestoreRecyclerOptions<User> options, Context context) {
        super(options);
        this.context = context;
    }

    @NonNull
    @Override
    public SearchUserRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_items,parent,false);
        return new SearchUserRecyclerViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull SearchUserRecyclerViewHolder holder, int position, @NonNull User model) {

        Log.d("UserName", model.getUserName());

        if(model.getUserId().equals(FirebaseUtil.getCurrentUserId())){
            holder.name.setText(model.getName() + " (You)");
        }
        else{
            holder.name.setText(model.getName());
        }

        holder.username.setText(model.getUserName());

        setPicture(holder.pfp, Uri.parse(model.getPfp()), holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("User",model);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    public class SearchUserRecyclerViewHolder extends RecyclerView.ViewHolder {

        ImageView pfp;
        ProgressBar pfpProgressBar;
        TextView name, username;


        public SearchUserRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            pfp = itemView.findViewById(R.id.profilePic);
            name = itemView.findViewById(R.id.name);
            username = itemView.findViewById(R.id.userName);
            pfpProgressBar = itemView.findViewById(R.id.pfpProgressBar);
        }
    }

    private void setPicture(ImageView imageView, Uri imageUri, SearchUserRecyclerViewHolder holder){

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
