package com.example.chatx.View.Main.Home;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatx.Adapter.SearchUserRecyclerAdapter;
import com.example.chatx.Model.DataModels.User;
import com.example.chatx.ViewModel.ReadWriteViewModel;
import com.example.chatx.databinding.ActivitySearchUserBinding;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

public class SearchUserActivity extends AppCompatActivity {

    private ActivitySearchUserBinding xml;
    private SearchUserRecyclerAdapter adapter;

    private ReadWriteViewModel readWriteViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //root
        xml = ActivitySearchUserBinding.inflate(getLayoutInflater());
        setContentView(xml.getRoot());

        readWriteViewModel = ReadWriteViewModel.getInstance(getApplication());

        xml.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        xml.searchBarLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callQuery(xml.searchBarEditText.getText().toString().trim());
            }
        });

        xml.searchBarEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // The "Done" key on the keyboard was pressed
                    callQuery(xml.searchBarEditText.getText().toString().trim());
                    return true;
                }
                return false;
            }
        });
    }

    private void callQuery(String searchedUser){
        readWriteViewModel.getUsers(searchedUser).observe(this, new Observer<Query>() {
            @Override
            public void onChanged(Query query) {
                setUpAdapter(query);
            }
        });
    }

    private void setUpAdapter(Query query){

        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class).build();

        adapter = new SearchUserRecyclerAdapter(options,getApplicationContext());
        xml.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        xml.recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    protected void onStart() {
        super.onStart();

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

    @Override
    protected void onResume() {
        super.onResume();

        if(adapter!=null){
            adapter.notifyDataSetChanged();
        }
    }
}