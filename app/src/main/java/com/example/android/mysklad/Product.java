package com.example.android.mysklad;

import java.util.HashMap;
import java.util.Map;

public class Product {
    private String id, title, photoUrl;
    private float price;

    public Product(){

    }

    public Product(String name, float price) {
        this.title = name;
        this.price = price;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("title", title);
        result.put("photoUrl", photoUrl);
        result.put("price", price);

        return result;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}
