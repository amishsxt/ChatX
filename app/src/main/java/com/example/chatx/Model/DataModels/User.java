package com.example.chatx.Model.DataModels;

import java.io.Serializable;

public class User implements Serializable {

    private String name, userName, email, pfp, userId, fcmToken;

    public User() {
    }

    public User(String name, String userName, String email, String pfp) {
        this.name = name;
        this.userName = userName;
        this.email = email;
        this.pfp = pfp;
    }

    public String getUserId() {
        return userId;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPfp() {
        return pfp;
    }

    public void setPfp(String pfp) {
        this.pfp = pfp;
    }
}
