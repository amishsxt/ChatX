package com.example.chatx.Model.DataModels;

import com.google.firebase.Timestamp;

import java.util.List;

public class ChatRoom {

    private String chatRoomId;
    private List<String> userIds;
    private Timestamp lastMsgTimeStamp;
    private String lastMsgSenderId;
    private String lastMessage;

    public ChatRoom() {
    }

    public ChatRoom(String chatRoomId, List<String> userIds, Timestamp lastMsgTimeStamp, String lastMsgSenderId) {
        this.chatRoomId = chatRoomId;
        this.userIds = userIds;
        this.lastMsgTimeStamp = lastMsgTimeStamp;
        this.lastMsgSenderId = lastMsgSenderId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public Timestamp getLastMsgTimeStamp() {
        return lastMsgTimeStamp;
    }

    public void setLastMsgTimeStamp(Timestamp lastMsgTimeStamp) {
        this.lastMsgTimeStamp = lastMsgTimeStamp;
    }

    public String getLastMsgSenderId() {
        return lastMsgSenderId;
    }

    public void setLastMsgSenderId(String lastMsgSenderId) {
        this.lastMsgSenderId = lastMsgSenderId;
    }
}
