    package com.example.myapplication;

import android.app.Service;
import android.media.session.PlaybackState;

import androidx.core.app.ServiceCompat;

import static com.example.myapplication.PlayService.notificationLayout;

public class User {

    public static Service service;

    public static void getService(Service serviceAt){
        service = serviceAt;
    }

    public static void changeNotificationToPlay() {
//        PlayService.notificationLayout.setTextViewText(R.id.btn, "play");
//        PlayService.mPlaybackState.setState( PlaybackState.STATE_PLAYING, PlayService.m.getCurrentPosition(), 0f);
        PlayService.mediaSession.setPlaybackState(PlayService.getPBS(0f,   PlayService.m.getCurrentPosition()));
        ServiceCompat.stopForeground(service,0);
        service.startForeground(121,PlayService.customNotification);
        ServiceCompat.stopForeground(service,0);
    }

    public static void changeNotificationToPause() {
//        PlayService.m.getCurrentPosition()
//        PlayService.notificationLayout.setTextViewText(R.id.btn, "pause");
        PlayService.mediaSession.setPlaybackState(PlayService.getPBS(1.0f,  PlayService.m.getCurrentPosition()));
        service.startForeground(121,PlayService.customNotification);

    }
}