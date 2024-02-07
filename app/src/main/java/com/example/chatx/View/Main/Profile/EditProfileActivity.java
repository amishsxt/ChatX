package com.example.chatx.View.Main.Profile;

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
import com.example.chatx.Utils.ProgressDialogFragment;
import com.example.chatx.ViewModel.ReadWriteViewModel;
import com.example.chatx.databinding.ActivityEditProfileBinding;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding xml;
    private User currentUser;
    private ReadWriteViewModel readWriteViewModel;
    private Uri imageUri;

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int PICK_IMAGE_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //root
        xml = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(xml.getRoot());

        //getting intent (user obj)
        currentUser = (User) getIntent().getSerializableExtra("User");
        setUserData(currentUser);
        readWriteViewModel = ReadWriteViewModel.getInstance(getApplication());

        xml.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        xml.profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        ActivityCompat.requestPermissions(EditProfileActivity.this,
                                new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                    else{
                        ActivityCompat.requestPermissions(EditProfileActivity.this,
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

        xml.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!xml.nameEditText.getText().toString().isBlank()){
                    currentUser.setName(xml.nameEditText.getText().toString());
                }

                updateData(currentUser);
            }
        });
    }

    private void setUserData(User user){
        xml.nameEditText.setText(user.getName());
        setPicture(xml.profilePic, Uri.parse(user.getPfp()));

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

    private void updateData(User user){

        showProgressDialog();

        readWriteViewModel.updateUserData(user, imageUri, new AuthCallback() {
            @Override
            public void onSuccess() {
                hideProgressDialog();
                finish();
            }

            @Override
            public void onFailure(String ex) {
                hideProgressDialog();
                Toast.makeText(EditProfileActivity.this, "update failed: "+ ex, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgressDialog(){
        ProgressDialogFragment progressDialog = new ProgressDialogFragment();
        progressDialog.show(getSupportFragmentManager(), "progress_dialog_tag");
    }

    private void hideProgressDialog(){
        ProgressDialogFragment progressDialog = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag("progress_dialog_tag");
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
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