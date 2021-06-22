package com.example.myapplication;

import android.graphics.Bitmap;

public class DataModel {
    String title;
    Bitmap imageId;

    public DataModel(String title, Bitmap imageId) {
        this.title = title;
        this.imageId = imageId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Bitmap getBitmap() {
        return imageId;
    }

}