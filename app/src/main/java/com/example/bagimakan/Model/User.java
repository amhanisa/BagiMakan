package com.example.bagimakan.Model;

import com.google.firebase.firestore.Exclude;

public class User {

    private String kontak;
    private String key;

    public User() {
    }

    public User(String kontak) {
        this.kontak = kontak;
    }

    @Exclude
    public String getKey() {
        return key;
    }

    @Exclude
    public void setKey(String key) {
        this.key = key;
    }

    public String getKontak() {
        return kontak;
    }

    public void setKontak(String kontak) {
        this.kontak = kontak;
    }


}
