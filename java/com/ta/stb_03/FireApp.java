package com.ta.stb_03;

import android.app.Application;

import com.firebase.client.Firebase;

public class FireApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Firebase.setAndroidContext(this);
    }
}
