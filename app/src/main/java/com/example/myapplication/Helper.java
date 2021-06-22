package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ServiceCompat;

import com.example.myapplication.ui.home.HomeFragment;

import static com.example.myapplication.PlayService.notificationLayout;



public class Helper extends BroadcastReceiver {
    @SuppressLint("StaticFieldLeak")
    static Service service;

    public static void getService(Service serviceAt){
        service = serviceAt;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (PlayService.m.isPlaying()) {
            User.changeNotificationToPlay();
            HomeFragment.pause.setText("play");
            PlayService.m.pause();
        } else {
            User.changeNotificationToPause();
            HomeFragment.pause.setText("pause");
            PlayService.m.start();

        }
    }
}

