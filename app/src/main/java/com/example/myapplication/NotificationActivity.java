package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class NotificationActivity extends AppCompatActivity {
    TextView txt;
    public static Button btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_notification_acitivity);
        txt = findViewById(R.id.txt);
        btn = findViewById(R.id.btn);
        PlayService.m.pause();
//       custom activity notification
//        String action = (String) getIntent().getExtras().get("do_action");
        Intent intent = getIntent();
        String action = intent.getAction();


        switch (action) {
            case "pause" :
                if (PlayService.m.isPlaying()) {
                    PlayService.m.pause();
                } else {
                    PlayService.m.start();
                }
                break;
            case "play" :
                PlayService.m.pause();
                break;
        }
//
//        if (action != null) {
//
//            if (action.equals("play")) {
//
//            } else if (action.equals("close")) {
//                // close current notification
//            }
//        }
//
//        finish();


    }
}