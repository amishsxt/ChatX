package com.example.chatx.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.chatx.Model.DataModels.ChatRoom;
import com.example.chatx.Model.DataModels.User;
import com.example.chatx.Model.Repository.ChatRepo;
import com.example.chatx.Utils.Callbacks.OnChatRoomDataRetrievedListener;
import com.example.chatx.Utils.Callbacks.OnCompleteCallback;
import com.example.chatx.Utils.Callbacks.OnUserDataRetrievedListener;
import com.google.firebase.firestore.Query;

import java.util.List;

public class ChatViewModel extends AndroidViewModel {

    private static ChatViewModel instance;
    private ChatRepo chatRepo;

    private ChatViewModel(@NonNull Application application) {
        super(application);

        chatRepo = ChatRepo.getInstance(application);
    }

    public static synchronized ChatViewModel getInstance(@NonNull Application application){
        if (instance == null){
            instance = new ChatViewModel(application);
        }

        return instance;
    }

    public void getChatRoomData(String otherUserId, final OnChatRoomDataRetrievedListener listener){
        chatRepo.getChatRoomData(otherUserId, listener);
    }

    public void sendMessageToChat(ChatRoom chatRoom, String msg, User otherUser, OnCompleteCallback callback){
        chatRepo.sendMessageToChat(chatRoom, msg, otherUser, callback);
    }

    public LiveData<Query> getMessageFromChat(String chatRoomId){
        return chatRepo.getMessageFromChat(chatRoomId);
    }

    public LiveData<Query> getAllRecentChats(){
        return chatRepo.getAllRecentChats();
    }

    public void getThisUserData(List<String> uIds, OnUserDataRetrievedListener listener){
        chatRepo.getThisUserData(uIds, listener);
    }
}
