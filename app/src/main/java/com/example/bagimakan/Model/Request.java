package com.example.bagimakan.Model;

import com.google.firebase.firestore.Exclude;

public class Request {

    private String userId;
    private String userName;
    private Integer jumlah;
    private String status;
    private String key;

    public Request(){}

    public Request(String userId, String userName, Integer jumlah, String status) {
        this.userId = userId;
        this.userName = userName;
        this.jumlah = jumlah;
        this.status = status;
    }

    @Exclude
    public String getKey() {
        return key;
    }

    @Exclude
    public void setKey(String key) {
        this.key = key;
    }

    public String getUserId() {
        return userId;
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

    public Integer getJumlah() {
        return jumlah;
    }

    public void setJumlah(Integer jumlah) {
        this.jumlah = jumlah;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
