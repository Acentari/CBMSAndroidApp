package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static com.example.myapplication.ui.home.HomeFragment.path;
import static com.example.myapplication.ui.home.HomeFragment.pause;
import static com.example.myapplication.ui.home.HomeFragment.seek;
import static com.example.myapplication.ui.home.HomeFragment.trackName;


public class PlayService extends Service {
    public static MediaPlayer m;
    public static RemoteViews notificationLayout;
    public static Notification customNotification;
    public static PlaybackState mPlaybackState;
    public static MediaSession mediaSession;
    @SuppressLint("WrongConstant")
    @Override
    public void onCreate() {
        m = new MediaPlayer();

        m.setAudioAttributes( new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());


        m.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }

        });

        User.getService(PlayService.this);

//        PlayService.this.getCacheDir()

        String url ;
//        String path = Environment.getExternalStorageDirectory().getPath()+"/CBMS";

//        pause.setEnabled(true);
//        seek.setEnabled(true);
        url = path + "/" + trackName;

        try {
            m.setDataSource(url);
            m.prepare();
            m.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        seek.setMax(m.getDuration()/1000);


        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? getNotificationChannel(notificationManager) : "";
        Intent playIntent = new Intent(this, Helper.class);
        Intent mainActivity = new Intent(this, MainActivity.class);
        playIntent.setAction("play");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            Intent pauseIntent = new Intent(this, Helper.class);
            pauseIntent.setAction("pause");
            MediaMetadata.Builder mediaMetaData_builder = new MediaMetadata.Builder();
            mediaMetaData_builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION,m.getDuration());
            mediaSession = new MediaSession(this, "something");
            mediaSession.setActive(true);
            mediaSession.setMetadata(mediaMetaData_builder.build());
            mediaSession.setPlaybackState(getPBS(1.0f,  m.getCurrentPosition()));

            MediaSession.Callback callback = new MediaSession.Callback() {

                @Override
                public void onSeekTo(long pos) {
                    super.onSeekTo(pos);
                    m.seekTo((int)pos);

                    if (!m.isPlaying()) {
                        mediaSession.setPlaybackState(PlayService.getPBS(0f,   PlayService.m.getCurrentPosition()));
                    } else {
                        mediaSession.setPlaybackState(PlayService.getPBS(1.0f,   PlayService.m.getCurrentPosition()));
                    }
                }

                @Override
                public void onPlay() {
                    if (PlayService.m.isPlaying()) {
                        User.changeNotificationToPlay();
                        pause.setText("play");
                        PlayService.m.pause();
                    } else {
                        User.changeNotificationToPause();
                        pause.setText("pause");
                        PlayService.m.start();

                    }
                }
            };
            mediaSession.setCallback(callback);


            PendingIntent playPausePendingIntent =  PendingIntent.getBroadcast(getApplicationContext(), 1,pauseIntent,0);
            customNotification = new Notification.Builder(this,channelId)
                    .setSmallIcon(R.drawable.m)
                    .setContentTitle("Now Playing...")
                    .setContentText(MainActivity.trackName)
                    .setLargeIcon( Icon.createWithResource(getApplicationContext(),R.drawable.m))
                    .addAction(new Notification.Action.Builder(
                            Icon.createWithResource(getApplicationContext(),R.drawable.a),
                            "Play/Pause",
                            playPausePendingIntent).build())
                    .setStyle(new Notification.MediaStyle()
                            .setMediaSession(mediaSession.getSessionToken()))
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setContentIntent( PendingIntent.getActivity(getApplicationContext(), 3,mainActivity,0))
                    .setColor(Color.BLACK)
                    .build();
            startForeground(121, customNotification);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String getNotificationChannel(NotificationManager notificationManager){
        String channelId = "channelid";
        String channelName = getResources().getString(R.string.app_name);
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



        return START_STICKY;
    }


    public void onDestroy() {
        if (m.isPlaying()) {
            m.stop();
        }
        m.release();
        stopForeground(true);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("WrongConstant")
    public static PlaybackState getPBS(float f, int pos) {
        return new PlaybackState.Builder()
                .setState( PlaybackState.STATE_PLAYING,pos, f)
                .setActions( PlaybackState.ACTION_PLAY |
                        PlaybackState.ACTION_PAUSE |
                        PlaybackState.ACTION_SKIP_TO_NEXT |
                        PlaybackState.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackState.ACTION_SEEK_TO )
                .build();

    }
}