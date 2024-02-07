package com.example.chatx.Model.Repository;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.chatx.Model.DataModels.User;
import com.example.chatx.Utils.Callbacks.AuthCallback;
import com.example.chatx.Utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class ReadWriteRepo {

    private static ReadWriteRepo instance;
    private Application application;
    private FirebaseAuth mAuth;


    private ReadWriteRepo(Application application) {
        this.application = application;

        mAuth = FirebaseAuth.getInstance();
    }

    public static synchronized ReadWriteRepo getInstance(Application application){
        if (instance == null){
            instance = new ReadWriteRepo(application);
        }

        return instance;
    }

    public LiveData<User> getUserData(){

        MutableLiveData<User> mutableLiveData = new MutableLiveData<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference docRef = db.collection("Users").document(mAuth.getCurrentUser().getUid());

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.d("getUserData", "Listen failed: "+ e.getMessage());
                    return;
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {
                    Log.d("getUserData", source + " data: " + snapshot.getData());

                    User user = snapshot.toObject(User.class);
                    mutableLiveData.setValue(user);
                } else {
                    Log.d("getUserData", source + " data: null");
                }
            }
        });

        return mutableLiveData;
    }

    public void updateUserData(User user, Uri imageUri, AuthCallback callback){

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference userRef = db.collection("Users").document(mAuth.getCurrentUser().getUid());

        // Create a Map to store the user data
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", user.getName());
        userData.put("userName", user.getUserName());
        userData.put("email", user.getEmail());
        userData.put("pfp", user.getPfp());

        // Set the user data in the document
        userRef.set(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Data saved successfully
                        Log.d("saveUserData", "User data saved successfully");

                        if(imageUri !=null && !user.getPfp().equals(String.valueOf(imageUri))){
                            uploadImage(imageUri, userRef, callback);
                        }
                        else{
                            callback.onSuccess();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error saving data
                        Log.w("saveUserData", "Error: " + e.getMessage().toString());
                    }
                });
    }

    private void uploadImage(Uri imageUri, DocumentReference reference, AuthCallback callback) {

        StorageReference fileReference = FirebaseStorage.getInstance().getReference("Users pfp")
                .child(mAuth.getCurrentUser().getUid());

        fileReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if(task.isSuccessful()){
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            reference.update("pfp", String.valueOf(uri))
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            callback.onSuccess();
                                            Log.d("uploadImage", "Image Uploaded successfully");
                                        }
                                    });
                        }
                    });
                }
                else {
                    callback.onFailure("Picture upload failed!");
                    callback.onSuccess();
                }

            }
        });
    }

    public LiveData<Query> getUsers(String searchedUser){

        MutableLiveData<Query> mutableLiveData = new MutableLiveData<>();

        Query query =  FirebaseUtil.allUserCollectionRef().whereEqualTo("userName", searchedUser);

        Task<QuerySnapshot> task = query.get();
        task.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot snapshot = task.getResult();
                    mutableLiveData.setValue(query);

                    if (snapshot != null && !snapshot.isEmpty()) {
                        Log.d("SearchUserQuery", "Query has results: " + snapshot.size());
                    } else {
                        Log.d("SearchUserQuery", "Query is empty");
                    }
                } else {
                    Log.d("SearchUserQuery", "Query failed");
                }
            }
        });

        return mutableLiveData;
    }

    public void updateFcmToken(String token){
        FirebaseUtil.currentUserDetails().update("fcmToken",token)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d("updateFcmToken", "Token updated");
                        }
                        else{
                            Log.d("updateFcmToken", "Token updation failed");
                        }
                    }
                });
    }
}
