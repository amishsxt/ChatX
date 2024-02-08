package com.example.chatx.View.SplashScreen;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatx.Model.DataModels.User;
import com.example.chatx.R;
import com.example.chatx.Utils.FirebaseUtil;
import com.example.chatx.View.Auth.LogInActivity;
import com.example.chatx.View.Main.Chat.ChatActivity;
import com.example.chatx.View.Main.LandingActivity;
import com.example.chatx.ViewModel.AuthViewModel;
import com.example.chatx.ViewModel.ReadWriteViewModel;
import com.example.chatx.databinding.ActivitySplashBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding xml;

    private AuthViewModel authViewModel;
    private ReadWriteViewModel readWriteViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //root
        xml =ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(xml.getRoot());

        //init
        authViewModel = AuthViewModel.getInstance(getApplication());

        // Fade-in animation
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeInAnimation.setDuration(800);

        fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                checkLoginStatus();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        xml.splashText.startAnimation(fadeInAnimation);
    }

    private void fcmIntent(){
        String senderUserId = getIntent().getExtras().getString("userId");
        FirebaseUtil.allUserCollectionRef().document(senderUserId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            User user = task.getResult().toObject(User.class);

                            Intent mainIntent = new Intent(SplashActivity.this, LandingActivity.class);
                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(mainIntent);

                            Intent intent = new Intent(SplashActivity.this, ChatActivity.class);
                            intent.putExtra("User",user);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

                            finish();
                        }
                    }
                });
    }

    private void checkLoginStatus(){

        // Get the SharedPreferences instance
        SharedPreferences sharedPreferences = getSharedPreferences("ChatXAuthPrefs", Context.MODE_PRIVATE);

        // Retrieve the login status
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        // Check the login status
        if (isLoggedIn) {

            readWriteViewModel = ReadWriteViewModel.getInstance(getApplication());

            if(getIntent().getExtras()!=null){
                fcmIntent();
            }
            else {
                Intent intent = new Intent(SplashActivity.this, LandingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } else {
            // The user is not logged in

            Intent intent = new Intent(SplashActivity.this, LogInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

    }

}