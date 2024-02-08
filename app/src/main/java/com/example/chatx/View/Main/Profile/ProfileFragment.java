package com.example.chatx.View.Main.Profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.example.chatx.Model.DataModels.User;
import com.example.chatx.R;
import com.example.chatx.Utils.Callbacks.AuthCallback;
import com.example.chatx.View.Auth.LogInActivity;
import com.example.chatx.ViewModel.AuthViewModel;
import com.example.chatx.ViewModel.ReadWriteViewModel;
import com.example.chatx.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding xml;
    private ReadWriteViewModel readWriteViewModel;
    private AuthViewModel authViewModel;
    private User currentUser;

    private AlertDialog.Builder builder;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        xml = FragmentProfileBinding.inflate(inflater, container, false);


        readWriteViewModel = ReadWriteViewModel.getInstance(requireActivity().getApplication());
        authViewModel = AuthViewModel.getInstance(requireActivity().getApplication());
        builder = new AlertDialog.Builder(getContext());

        getUserData();

        xml.logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLogoutDialog();
            }
        });

        xml.editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), EditProfileActivity.class);
                intent.putExtra("User", currentUser);
                startActivity(intent);
            }
        });

        return xml.getRoot();
    }

    private void showLogoutDialog(){
        //logOut logic
        builder.setTitle("Log Out")
                .setMessage("Are you sure you want to logout?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteFCMToken();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .show();
    }

    private void logoutUser(){

        authViewModel.logOutUser(new AuthCallback() {
            @Override
            public void onSuccess() {
                writeLoginStatus(false);

                Intent intent = new Intent(getContext(), LogInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            @Override
            public void onFailure(String ex) {
                Toast.makeText(getContext(), ex, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void writeLoginStatus(boolean bool){
        // Get the SharedPreferences instance
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("ChatXAuthPrefs", Context.MODE_PRIVATE);

        // Write the login status
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", bool);
        editor.apply();
    }

    private void getUserData(){

        readWriteViewModel.getUserData().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                currentUser = user;
                setUserData(currentUser);
            }
        });
    }

    private void setUserData(User user){

        xml.name.setText(user.getName());
        xml.userName.setText(user.getUserName());

        setPicture(xml.profilePic, Uri.parse(user.getPfp()));

    }

    private void deleteFCMToken(){
        FirebaseMessaging.getInstance().deleteToken()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.d("deleteFCMToken", "Token deleted");
                        }
                        else{
                            Log.d("deleteFCMToken", "Token deletion failed");
                        }
                    }
                });

        logoutUser();
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
}