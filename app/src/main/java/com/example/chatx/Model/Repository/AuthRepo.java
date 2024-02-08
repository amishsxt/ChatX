package com.example.chatx.Model.Repository;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.chatx.Model.DataModels.User;
import com.example.chatx.Utils.Callbacks.AuthCallback;
import com.example.chatx.Utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class AuthRepo {

    private static AuthRepo instance;
    private Application application;
    private FirebaseAuth mAuth;

    private AuthRepo(Application application) {
        this.application = application;

        mAuth = FirebaseAuth.getInstance();
    }

    public static synchronized AuthRepo getInstance(Application application){
        if(instance == null){
            instance = new AuthRepo(application);
        }

        return instance;
    }


    public void loginUser(String email, String password, AuthCallback authCallback){

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d("userLogin", "login success");
                            authCallback.onSuccess();
                        }
                        else {
                            Log.d("userLogin", "login failed");
                            Log.d("login failed error", task.getException().getMessage().toString());


                            if(task.getException() instanceof FirebaseAuthInvalidUserException){
                                authCallback.onFailure("Email is not registered");
                            }
                            else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                authCallback.onFailure("Incorrect password or email");
                            } else {
                                authCallback.onFailure(task.getException().getMessage().toString());
                            }
                        }
                    }
                });
    }

    public void registerUser(User user, String password, AuthCallback authCallback){

        mAuth.createUserWithEmailAndPassword(user.getEmail(), password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d("userSignUp", "signup success");
                            mAuth = FirebaseAuth.getInstance();
                            saveUserData(user, authCallback);
                        }
                        else {
                            Log.d("userSignUp", "signup failed");
                            Log.d("signup failed error", task.getException().getMessage().toString());

                            authCallback.onFailure(task.getException().getMessage().toString());
                        }
                    }
                });
    }

    public void logOutUser(AuthCallback authCallback){

        Task<Void> singOutTask = Tasks.call(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mAuth.signOut();
                return null;
            }
        });

        singOutTask.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d("logOutStatus","success");
                    authCallback.onSuccess();
                }
                else{
                    Log.d("logOutStatus","failed: " + task.getException().getMessage().toString());
                    authCallback.onFailure(task.getException().getMessage().toString());
                }
            }
        });

    }

    private void saveUserData(User user, AuthCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference userRef = db.collection("Users").document(mAuth.getCurrentUser().getUid());

        // Create a Map to store the user data
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", user.getName());
        userData.put("userName", user.getUserName());
        userData.put("email", user.getEmail());
        userData.put("pfp", user.getPfp());
        userData.put("userId", mAuth.getCurrentUser().getUid().toString());

        // Set the user data in the document
        userRef.set(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Data saved successfully
                        Log.d("saveUserData", "User data saved successfully");
                        uploadImage(Uri.parse(user.getPfp()), userRef, callback);
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

    public void uploadImage(Uri imageUri, DocumentReference reference, AuthCallback callback) {

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
}
