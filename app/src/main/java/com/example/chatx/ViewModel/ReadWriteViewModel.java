package com.example.chatx.ViewModel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.chatx.Model.DataModels.User;
import com.example.chatx.Model.Repository.ReadWriteRepo;
import com.example.chatx.Utils.Callbacks.AuthCallback;
import com.google.firebase.firestore.Query;

public class ReadWriteViewModel extends AndroidViewModel {

    private static ReadWriteViewModel instance;
    private ReadWriteRepo readWriteRepo;

    private ReadWriteViewModel(@NonNull Application application) {
        super(application);

        readWriteRepo = ReadWriteRepo.getInstance(application);
    }

    public static synchronized ReadWriteViewModel getInstance(@NonNull Application application){
        if (instance == null){
            instance = new ReadWriteViewModel(application);
        }

        return instance;
    }

    public LiveData<User> getUserData(){
        return readWriteRepo.getUserData();
    }

    public void updateUserData(User user, Uri imageUri,  AuthCallback callback){
        readWriteRepo.updateUserData(user, imageUri, callback);
    }

    public LiveData<Query> getUsers(String searchedUser){
        return readWriteRepo.getUsers(searchedUser);
    }

    public void updateFcmToken(String token){
        readWriteRepo.updateFcmToken(token);
    }

}
