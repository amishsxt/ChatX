package com.example.chatx.View.Auth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.chatx.Model.DataModels.User;
import com.example.chatx.R;
import com.example.chatx.Utils.Callbacks.AuthCallback;
import com.example.chatx.ViewModel.AuthViewModel;
import com.example.chatx.databinding.ActivitySignUpBinding;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding xml;
    private AuthViewModel authViewModel;
    private Uri imageUri;

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int PICK_IMAGE_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //root
        xml=ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(xml.getRoot());

        authViewModel = AuthViewModel.getInstance(getApplication());
        //authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        xml.signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        xml.profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(SignUpActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        ActivityCompat.requestPermissions(SignUpActivity.this,
                                new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                    else{
                        ActivityCompat.requestPermissions(SignUpActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }

                } else {
                    // Permission already granted, proceed with accessing the content URI
                    openGallery();
                }
            }
        });

        xml.signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkSignUpPara();
            }
        });
    }

    private void checkSignUpPara(){
        showProgressBar();

        if(xml.nameEditText.getText().toString().isBlank()){
            xml.nameTextLayout.setError("Enter name");
            hideProgressBar();
        } else if (xml.userNameEditText.getText().toString().isBlank()) {
            xml.userNameTextLayout.setError("Enter username");
            hideProgressBar();
        } else if (xml.emailEditText.getText().toString().isBlank()) {
            xml.emailTextLayout.setError("Enter email");
            hideProgressBar();
        } else if (xml.passwordEditText.getText().length() < 8) {
            xml.passwordTextLayout.setError("Password should be of min 8 characters");
            hideProgressBar();
        }
        else if (imageUri == null){
            Toast.makeText(this, "Please set a pfp", Toast.LENGTH_SHORT).show();
            hideProgressBar();
        }
        else {

            User user = new User(String.valueOf(xml.nameEditText.getText())
                    , String.valueOf(xml.userNameEditText.getText())
                    , String.valueOf(xml.emailEditText.getText())
                    , String.valueOf(imageUri));

            startSignUp(user, String.valueOf(xml.passwordEditText.getText()));
        }
    }

    private void startSignUp(User user, String password){

        authViewModel.registerUser(user, password, new AuthCallback() {
            @Override
            public void onSuccess() {
                hideProgressBar();

                Toast.makeText(SignUpActivity.this, "SignUp successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            @Override
            public void onFailure(String ex) {
                Toast.makeText(SignUpActivity.this, "SignUp failed!", Toast.LENGTH_SHORT).show();
                hideProgressBar();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            // Check if the permission was granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with accessing the content URI
                openGallery();
            } else {
                // Permission denied, handle accordingly (e.g., show an error message)
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            setPicture(xml.profilePic,imageUri);

        }
    }

    private void openGallery() {
        Intent photoIntent = new Intent(Intent.ACTION_PICK);
        photoIntent.setType("image/*");
        startActivityForResult(photoIntent, PICK_IMAGE_REQUEST_CODE);
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

    private void showProgressBar(){
        xml.signUpBtn.setVisibility(View.GONE);
        xml.progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        xml.signUpBtn.setVisibility(View.VISIBLE);
        xml.progressBar.setVisibility(View.GONE);
    }

    private void showPfpProgressBar(){
        xml.profilePic.setVisibility(View.GONE);
        xml.pfpProgressBar.setVisibility(View.VISIBLE);
    }

    private void hidePfpProgressBar(){
        xml.profilePic.setVisibility(View.VISIBLE);
        xml.pfpProgressBar.setVisibility(View.GONE);
    }
}