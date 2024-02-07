package com.example.chatx.View.Main;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.chatx.R;
import com.example.chatx.View.Main.Home.HomeFragment;
import com.example.chatx.View.Main.Profile.ProfileFragment;
import com.example.chatx.ViewModel.ReadWriteViewModel;
import com.example.chatx.databinding.ActivityLandingBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.messaging.FirebaseMessaging;

public class LandingActivity extends AppCompatActivity {

    private ActivityLandingBinding xml;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //root
        xml=ActivityLandingBinding.inflate(getLayoutInflater());
        setContentView(xml.getRoot());

        xml.bottomNavBar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if(item.getItemId() == R.id.nav_home){
                    loadFragment(new HomeFragment());
                    return true;
                } else if (item.getItemId() == R.id.nav_profile) {
                    loadFragment(new ProfileFragment());
                    return true;
                }
                else{
                    loadFragment(new HomeFragment());
                    return false;
                }
            }
        });

        getFCMToken();

        // Load the default fragment
        loadFragment(new HomeFragment());

    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    private void getFCMToken(){
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()){
                    String token = task.getResult();
                    Log.i("MyToken",token);

                    ReadWriteViewModel.getInstance(getApplication()).updateFcmToken(token);
                }
            }
        });
    }
}