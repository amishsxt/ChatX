package com.example.chatx.Utils;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.List;

public class FirebaseUtil {

    public static boolean isLoggedIn(){
        if(getCurrentUserId()!=null){
            return true;
        }

        return false;
    }

    public static String getCurrentUserId() {
        return FirebaseAuth.getInstance().getUid();
    }

    public static FirebaseFirestore getDb() {
        return FirebaseFirestore.getInstance();
    }

    public static DocumentReference currentUserDetails(){
        return FirebaseFirestore.getInstance().collection("Users").document(getCurrentUserId());
    }

    public static CollectionReference allUserCollectionRef(){
        return getDb().collection("Users");
    }

    public static DocumentReference getChatRoomRef(String chatRoomId){

        return getDb().collection("ChatRooms").document(chatRoomId);
    }

    public static CollectionReference getChatRoomMessageRef(String chatroomId){
        return getChatRoomRef(chatroomId).collection("Chats");
    }

    public static String getChatRoomId(String otherUserId){

        if(getCurrentUserId().hashCode() < otherUserId.hashCode() ){
            return getCurrentUserId() + "_" + otherUserId;
        }
        else{
            return  otherUserId + "_" + getCurrentUserId();
        }
    }

    public static CollectionReference allChatRoomCollectionRef(){
        return FirebaseFirestore.getInstance().collection("ChatRooms");
    }

    public static DocumentReference getOtherUserFromChatroom(List<String> userIds){
        if(userIds.get(0).equals(FirebaseUtil.getCurrentUserId())){
            return allUserCollectionRef().document(userIds.get(1));
        }else{
            return allUserCollectionRef().document(userIds.get(0));
        }
    }

    public static String timestampToString(Timestamp timestamp){
        return new SimpleDateFormat("HH:MM").format(timestamp.toDate());
    }

    public static StorageReference getCurrentProfilePicStorageRef(){
        return FirebaseStorage.getInstance().getReference().child("Users pfp")
                .child(FirebaseUtil.getCurrentUserId());
    }

    public static StorageReference  getOtherProfilePicStorageRef(String otherUserId){
        return FirebaseStorage.getInstance().getReference().child("Users pfp")
                .child(otherUserId);
    }

}

