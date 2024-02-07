package com.example.chatx.Model.Repository;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.chatx.Model.DataModels.ChatMessage;
import com.example.chatx.Model.DataModels.ChatRoom;
import com.example.chatx.Model.DataModels.User;
import com.example.chatx.R;
import com.example.chatx.Utils.Callbacks.OnChatRoomDataRetrievedListener;
import com.example.chatx.Utils.Callbacks.OnCompleteCallback;
import com.example.chatx.Utils.Callbacks.OnUserDataRetrievedListener;
import com.example.chatx.Utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatRepo {

    private static ChatRepo instance;
    private Application application;
    private FirebaseAuth mAuth;


    private ChatRepo(Application application) {
        this.application = application;

        mAuth = FirebaseAuth.getInstance();
    }

    public static synchronized ChatRepo getInstance(Application application){
        if (instance == null){
            instance = new ChatRepo(application);
        }

        return instance;
    }

    public LiveData<Query> getAllRecentChats(){

        MutableLiveData<Query> mutableLiveData = new MutableLiveData<>();

        Query query = FirebaseUtil.allChatRoomCollectionRef()
                .whereArrayContains("userIds", mAuth.getUid())
                .orderBy("lastMsgTimeStamp", Query.Direction.DESCENDING);

        Task<QuerySnapshot> task = query.get();
        task.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot snapshot = task.getResult();
                    mutableLiveData.setValue(query);

                    if (snapshot != null && !snapshot.isEmpty()) {
                        Log.d("RecentChatQuery", "Query has results: " + snapshot.size());
                    } else {
                        Log.d("RecentChatQuery", "Query is empty");
                    }
                } else {
                    Log.d("RecentChatQuery", "Query failed: " + task.getException().getMessage());
                }
            }
        });

        return mutableLiveData;
    }

    public void getChatRoomData(String otherUserId, final OnChatRoomDataRetrievedListener listener) {
        String chatRoomId = FirebaseUtil.getChatRoomId(otherUserId);

        DocumentReference docRef = FirebaseUtil.getChatRoomRef(chatRoomId);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final ChatRoom chatRoom = task.getResult().toObject(ChatRoom.class);
                    if (chatRoom == null) {
                        final ChatRoom chatRoom2 = new ChatRoom(chatRoomId
                                , Arrays.asList(FirebaseUtil.getCurrentUserId(), otherUserId)
                                , Timestamp.now(), "");

                        docRef.set(chatRoom2).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("New chat", "created successfully");
                                } else {
                                    Log.e("New chat", "creation failed: " + task.getException().getMessage());
                                }
                                listener.onChatRoomDataRetrieved(chatRoom2);
                            }
                        });
                    } else {
                        listener.onChatRoomDataRetrieved(chatRoom);
                    }
                } else {
                    Log.e("ChatRoomData", "Error retrieving chat room data", task.getException());
                    listener.onChatRoomDataRetrieved(null); // Indicate error
                }
            }
        });
    }

    private void updateChatRoomData(ChatRoom chatRoom, String msg){

        DocumentReference docRef = FirebaseUtil.allChatRoomCollectionRef()
                .document(chatRoom.getChatRoomId());

        chatRoom.setLastMsgSenderId(FirebaseUtil.getCurrentUserId());
        chatRoom.setLastMsgTimeStamp(Timestamp.now());
        chatRoom.setLastMessage(msg);

        docRef.set(chatRoom).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d("Update ChatRoomData", "updated successfully");
                } else {
                    Log.e("Update ChatRoomData", "updation failed: " + task.getException().getMessage());
                }
            }
        });
    }

    public LiveData<Query> getMessageFromChat(String chatRoomId){

        MutableLiveData<Query> mutableLiveData = new MutableLiveData<>();

        Query query = FirebaseUtil.getChatRoomMessageRef(chatRoomId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        Task<QuerySnapshot> task = query.get();
        task.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot snapshot = task.getResult();
                    mutableLiveData.setValue(query);

                    if (snapshot != null && !snapshot.isEmpty()) {
                        Log.d("ChatQuery", "Query has results: " + snapshot.size());
                    } else {
                        Log.d("ChatQuery", "Query is empty");
                    }
                } else {
                    Log.d("ChatQuery", "Query failed");
                }
            }
        });

        return mutableLiveData;
    }

    public void getThisUserData(List<String> uIds, OnUserDataRetrievedListener listener){

        FirebaseUtil.getOtherUserFromChatroom(uIds).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    User user = task.getResult().toObject(User.class);
                    listener.onUserDataRetrievedListener(user);
                }
                else{
                    Log.d("getThisUser","task failed: "+task.getException().getMessage());
                }
            }
        });
    }

    public void sendMessageToChat(ChatRoom chatRoom, String msg, User otherUser, OnCompleteCallback callback){

        updateChatRoomData(chatRoom, msg);

        CollectionReference colRef = FirebaseUtil.getChatRoomMessageRef(chatRoom.getChatRoomId());

        ChatMessage chatMessage = new ChatMessage(msg, FirebaseUtil.getCurrentUserId(), Timestamp.now());

        colRef.add(chatMessage).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    Log.d("Update chat", "updated successfully");

                    sendNotification(msg,otherUser,callback);

                } else {
                    Log.e("Update chat", "updation failed: " + task.getException().getMessage());
                    callback.onError(task.getException().getMessage());
                }
            }
        });
    }

    private void sendNotification(String msg, User otherUser, OnCompleteCallback callback){
        FirebaseUtil.currentUserDetails().get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            User currentUser = task.getResult().toObject(User.class);

                            Log.d("senderName",currentUser.getName());

                            try{
                                JSONObject jsonObject = new JSONObject();

                                JSONObject notificationObj = new JSONObject();
                                notificationObj.put("title", currentUser.getName());
                                notificationObj.put("body", msg);

                                JSONObject dataObj = new JSONObject();
                                dataObj.put("userId",currentUser.getUserId());

                                jsonObject.put("notification",notificationObj);
                                jsonObject.put("data",dataObj);
                                jsonObject.put("to",otherUser.getFcmToken());

                                callApi(jsonObject, callback);

                            }catch (Exception ex){

                            }
                        }
                    }
                });
    }

    private void callApi(JSONObject jsonObject, OnCompleteCallback callback) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();

        String url = "https://fcm.googleapis.com/fcm/send";

        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        String fcmApiKey = application.getString(R.string.fcm_api_key);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", "Bearer " + fcmApiKey)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("callApi", "Api call failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                callback.onComplete();
                if(response.isSuccessful()){
                    Log.d("callApi", "Api called successfully");
                }
                else{
                    Log.d("callApi", "Api call failed: "+response.code());
                }

            }
        });
    }

}
