package com.example.chatx.View.Main.Home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatx.Adapter.HomeChatRecyclerAdapter;
import com.example.chatx.Model.DataModels.ChatRoom;
import com.example.chatx.ViewModel.ChatViewModel;
import com.example.chatx.databinding.FragmentHomeBinding;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding xml;
    private HomeChatRecyclerAdapter adapter;

    private ChatViewModel chatViewModel;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        xml = FragmentHomeBinding.inflate(inflater, container, false);

        chatViewModel = ChatViewModel.getInstance(requireActivity().getApplication());

        callQuery();

        xml.searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), SearchUserActivity.class));
            }
        });

        return xml.getRoot();
    }

    private void callQuery(){
        chatViewModel.getAllRecentChats().observe(this, new Observer<Query>() {
            @Override
            public void onChanged(Query query) {
                setUpAdapter(query);
            }
        });
    }

    private void setUpAdapter(Query query){

        FirestoreRecyclerOptions<ChatRoom> options = new FirestoreRecyclerOptions.Builder<ChatRoom>()
                .setQuery(query, ChatRoom.class).build();

        adapter = new HomeChatRecyclerAdapter(options,getContext(), chatViewModel);
        xml.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        xml.recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onStart() {
        super.onStart();

        if(adapter!=null){
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if(adapter!=null){
            adapter.stopListening();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(adapter!=null){
            adapter.notifyDataSetChanged();
        }
    }
}