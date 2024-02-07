package com.example.chatx.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.chatx.Model.DataModels.User;
import com.example.chatx.Model.Repository.AuthRepo;
import com.example.chatx.Utils.Callbacks.AuthCallback;

public class AuthViewModel extends AndroidViewModel {

    private static AuthViewModel instance;
    private AuthRepo authRepo;

    private AuthViewModel(@NonNull Application application) {
        super(application);

        authRepo = AuthRepo.getInstance(application);
    }

    public static synchronized AuthViewModel getInstance(@NonNull Application application){
        if(instance == null){
            instance = new AuthViewModel(application);
        }

        return instance;
    }

    public void loginUser(String email, String password, AuthCallback callback){

        authRepo.loginUser(email, password, callback);
    }

    public void registerUser(User user, String password, AuthCallback callback){

        authRepo.registerUser(user, password, callback);
    }

    public void logOutUser(AuthCallback callback){

        authRepo.logOutUser(callback);
    }
}
