package com.example.chatx.View.Auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatx.Utils.Callbacks.AuthCallback;
import com.example.chatx.View.Main.LandingActivity;
import com.example.chatx.ViewModel.AuthViewModel;
import com.example.chatx.databinding.ActivityLogInBinding;

public class LogInActivity extends AppCompatActivity {

    private ActivityLogInBinding xml;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //root
        xml=ActivityLogInBinding.inflate(getLayoutInflater());
        setContentView(xml.getRoot());

        authViewModel = AuthViewModel.getInstance(getApplication());

        xml.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkLoginPara();
            }
        });

        xml.signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LogInActivity.this, SignUpActivity.class));
            }
        });
    }

    private void checkLoginPara(){
        showProgressBar();

        if(xml.emailEditText.getText().toString().isBlank()){
            xml.emailTextLayout.setError("Enter email");
            hideProgressBar();
        }
        else if(xml.passwordEditText.getText().length() < 8){
            xml.passwordTextLayout.setError("Password should be of min 8 characters");
            hideProgressBar();
        }
        else{
            startLogin(xml.emailEditText.getText().toString(), xml.passwordEditText.getText().toString());
        }
    }

    private void startLogin(String email, String password){

        authViewModel.loginUser(email, password, new AuthCallback() {
            @Override
            public void onSuccess() {
                hideProgressBar();

                writeLoginStatus(true);

                Intent intent = new Intent(LogInActivity.this, LandingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            @Override
            public void onFailure(String ex) {
                Toast.makeText(LogInActivity.this, ex, Toast.LENGTH_SHORT).show();
                hideProgressBar();
            }
        });

    }

    private void showProgressBar(){
        xml.loginBtn.setVisibility(View.GONE);
        xml.progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(){
        xml.loginBtn.setVisibility(View.VISIBLE);
        xml.progressBar.setVisibility(View.GONE);
    }

    private void writeLoginStatus(boolean bool){
        // Get the SharedPreferences instance
        SharedPreferences sharedPreferences = getSharedPreferences("ChatXAuthPrefs", Context.MODE_PRIVATE);

        // Write the login status
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", bool);
        editor.apply();
    }
}