package com.example.bagimakan.Model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

public class Makanan {

    private String name;
    private Integer jumlah;
    private String lokasi;
    private Double lat;
    private Double lng;
    private String imageUrl;
    private String userName;
    private String userId;
    private Date date;
    private String kontak;
    private String key;

    public Makanan() {

    }

    public Makanan(String name, Integer jumlah, String lokasi, Double lat, Double lng, String imageUrl, String userName, String userId, Date date, String kontak) {
        this.name = name;
        this.jumlah = jumlah;
        this.lokasi = lokasi;
        this.lat = lat;
        this.lng = lng;
        this.imageUrl = imageUrl;
        this.userName = userName;
        this.userId = userId;
        this.date = date;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getJumlah() {
        return jumlah;
    }

    public void setJumlah(Integer jumlah) {
        this.jumlah = jumlah;
    }

    public String getLokasi() {
        return lokasi;
    }

    public void setLokasi(String lokasi) {
        this.lokasi = lokasi;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getKontak() {
        return kontak;
    }

    public void setKontak(String kontak) {
        this.kontak = kontak;
    }
}
