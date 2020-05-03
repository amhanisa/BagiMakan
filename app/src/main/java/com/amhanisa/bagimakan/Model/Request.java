package com.amhanisa.bagimakan.Model;

import com.google.firebase.firestore.Exclude;

public class Request {

    private String userId;
    private String userName;
    private String alasan;
    private Integer jumlah;
    private String status;
    private String key;

    public Request(){}

    public Request(String userId, String userName, String alasan, Integer jumlah, String status) {
        this.userId = userId;
        this.userName = userName;
        this.alasan = alasan;
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

    public String getAlasan() {
        return alasan;
    }

    public void setAlasan(String alasan) {
        this.alasan = alasan;
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
