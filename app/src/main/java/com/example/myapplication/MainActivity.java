
package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;

import android.app.ActivityManager;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.provider.SyncStateContract;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.commons.io.IOUtils;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.provider.ContactsContract.CommonDataKinds.StructuredName.PREFIX;
import static android.provider.ContactsContract.CommonDataKinds.StructuredName.SUFFIX;
import static androidx.core.content.ContextCompat.getSystemService;

public class MainActivity extends AppCompatActivity {
    Button upload;
    public static Button pause;
    public static TextView t;
    public static TextView txt;

    public static Intent serviceIntent;
    ListView tracksView;
    private boolean playing = false;
    public static boolean hasStarted = false;
    public static String username;
    List<DataModel> dataModelList;
    Bitmap largeIcon;
    List<String> tracks;
    MediaMetadataRetriever mmr;
    public static SeekBar seek;
    private Handler handler = new Handler();
    boolean touched = false;
    int trackProgress;
    public static String url;
    public static String trackName;
    File[] files;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        t = findViewById(R.id.t1);
        txt = findViewById(R.id.txt);
        tracksView = findViewById(R.id.all_tracks);
        upload = findViewById(R.id.upload);
        seek = findViewById(R.id.seekBar);
        Intent intent = getIntent();
        username = intent.getStringExtra("user");



        tracks = new ArrayList<>();
        dataModelList = new ArrayList<>();
        t.setText(username);
        pause = findViewById(R.id.stop);
        largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.m);
        serviceIntent = new Intent(getApplicationContext(), PlayService.class);
        pause.setEnabled(false);
        seek.setEnabled(false);

        if (isMyServiceRunning(PlayService.class)) {
            if (!PlayService.m.isPlaying()) {
                stopService(serviceIntent);
            } else {
                pause.setEnabled(true);
                seek.setEnabled(true);
                seek.setMax(PlayService.m.getDuration() / 1000);
            }
        }

        if (username == null) {
            SharedPreferences sp1=this.getSharedPreferences("Login", MODE_PRIVATE);
            username = sp1.getString("username", null);
        }


        upload.setOnClickListener(v -> {
            Intent intent1 = new Intent(getApplicationContext(),UploadActivity.class);
            intent1.putExtra("user", username);
            startActivity(intent1);
        });

        pause.setOnClickListener(v -> {
            if (PlayService.m.isPlaying()) {
                PlayService.m.pause();
                User.changeNotificationToPlay();
                pause.setText("play");
            } else {
                PlayService.m.start();
                User.changeNotificationToPause();
                pause.setText("pause");
            }
        });

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                trackProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                PlayService.m.seekTo(trackProgress*1000);
                if (PlayService.m.isPlaying()) {
                    PlayService.mediaSession.setPlaybackState(PlayService.getPBS(1.0f,   PlayService.m.getCurrentPosition()));
                } else {
                    PlayService.mediaSession.setPlaybackState(PlayService.getPBS(0f,   PlayService.m.getCurrentPosition()));
                }

            }
        });

        //sends data to server
        postData(username);
        String path = Environment.getExternalStorageDirectory().toString()+"/Music";
        File directory = new File(path);
        files = directory.listFiles();

//
//        File cacheDir = getApplicationContext().getCacheDir();


        final Runnable runnable = new Runnable() {
            public void run() {
                if (isMyServiceRunning(PlayService.class)) {
                    if(PlayService.m.isPlaying()){
                        int mCurrentPosition = PlayService.m.getCurrentPosition() / 1000;
                        seek.setProgress(mCurrentPosition);
                    }
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);

        tracksView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pause.setText("pause");
                trackName = tracks.get(position);
                boolean cached = false;

                for (File file : files) {
                    if (file.getName().equals(trackName)) {
                        cached = true;
                        break;
                    } else {
                        cached = false;
                    }
                }

                if (cached) {
                    if (isMyServiceRunning(PlayService.class)) {
                        stopService(serviceIntent);
                    }
                    startService(serviceIntent);
                } else {
                    executePost("http://192.168.1.108:8080/studentsapp-1.0-SNAPSHOT/Serv1?trackname="+trackName+"&username="+username,"",position);
                }

            }
        });
    }

    public void stream2file(InputStream in, int pos)  {

        File file =  new File( Environment.getExternalStorageDirectory().toString()+"/Music/"+tracks.get(pos));

        new Thread(new Runnable() {
            public void run() {
                try(OutputStream outputStream = new FileOutputStream(file)){
                    IOUtils.copy(in, outputStream);
                    if (!isMyServiceRunning(PlayService.class)) {
                        startService(serviceIntent);
                    } else {
                        stopService(serviceIntent);
                        startService(serviceIntent);
                    }
                } catch (IOException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            t.setText(e.toString());
                        }
                    });
                }
            }


        }).start();
    }

    public void executePost(String targetURL, String urlParameters, int pos) {


        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                //Create connection
                URL url = new URL(targetURL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");

                connection.setRequestProperty("Content-Length",
                        Integer.toString(urlParameters.getBytes().length));
                connection.setRequestProperty("Content-Language", "en-US");

                connection.setUseCaches(false);
                connection.setDoOutput(true);

                //Send request
                DataOutputStream wr = new DataOutputStream (connection.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.close();

                //Get Response
                InputStream is = connection.getInputStream();
                stream2file(is, pos);

            } catch (Exception e) {
                runOnUiThread(() -> t.setText(e.toString()));
                e.printStackTrace();
                connection.disconnect();
            }
        }).start();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!PlayService.m.isPlaying()) {
            stopService(serviceIntent);
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    public void okTracks(JSONArray JSONTracks, JSONArray JSONCovers) {
        List<String> covers = new ArrayList<>();


        new Thread(() -> {

            for (int i = 0; i < JSONTracks.length(); i++) {
                try {
                    tracks.add(JSONTracks.getString(i));
                    covers.add(JSONCovers.getString(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


            String fileName = "";
            boolean breakBool = false;
            if (files.length > 0) {
                for (int i = 0; i < tracks.size(); i++) {
                    for (File file : files) {
                        if (file.getName().equals(tracks.get(i))) {
                            fileName = file.getName();
                            breakBool = true;
                            break;
                        }

                        else { breakBool = false;}
                    }

                    if (breakBool) {
                        mmr = new MediaMetadataRetriever();
                        url = Environment.getExternalStorageDirectory().toString() + "/Music/" +fileName;
                        mmr.setDataSource(url);
                        byte[] img = mmr.getEmbeddedPicture();


                        if (img == null) {
                            dataModelList.add(new DataModel(fileName, largeIcon));
                        }
                        else { addDataModelList(fileName, img); }
                    }

                    else {
                        if (covers.get(i).equals("none")) {
                            dataModelList.add(new DataModel(tracks.get(i), largeIcon));
                        } else {
                            byte[] img = extractAlbumCoverFromJson(covers.get(i));
                            addDataModelList(tracks.get(i), img);
                        }
                    }

                }
            }

            else {
                for (int i = 0; i < tracks.size(); i++) {

                    if (covers.get(i).equals("none")) {
                        dataModelList.add(new DataModel(tracks.get(i), largeIcon));
                    } else {
                        byte[] img = extractAlbumCoverFromJson(covers.get(i));
                        addDataModelList(tracks.get(i), img);

                    }
                }
            }


            runOnUiThread(new Runnable() {
                public void run() {
                    tracksView.setAdapter(new MyAdapter(MainActivity.this, dataModelList));

                }
            });
        }).start();
    }

    private byte[] extractAlbumCoverFromJson(String cover) {
        List<Byte> covers1 = new ArrayList<>();
        String[] array = cover.split("\\[|,|\\]");
        for (int j = 1; j < array.length; j++) {
            covers1.add(Byte.parseByte(array[j]));
        }

        byte[] img = new byte[covers1.size()];

        for (int j = 0; j < covers1.size(); j++) {
            img[j] = covers1.get(j);
        }
        return img;
    }

    private void addDataModelList(String fileName,byte[] img) {
        InputStream inputStream  = new ByteArrayInputStream(img);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        dataModelList.add(new DataModel(fileName, bitmap));
    }

    public void postData(String username) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://192.168.1.108:8080/studentsapp-1.0-SNAPSHOT/Serv";

        StringRequest commonRequest = new StringRequest(Request.Method.GET, url+"?username="+username, response -> {
            //HANDLE RESPONSE
            JSONArray tracks = null;
            JSONArray covers = null;
            try {
                JSONObject obj = new JSONObject(response);
                tracks = obj.getJSONArray("tracks");
                covers = obj.getJSONArray("covers");
                okTracks(tracks , covers);

                Log.d("TAG", "onResponse: "+obj);
            } catch (JSONException e) {
                Log.d("TAG",response);
                e.printStackTrace();
            }
//                t.setText(response);

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


                t.setText("error");

            }
        }) {
            @Override
            protected Map<String, String> getParams(){

                return null;
            }
        };
        queue.add(commonRequest);
        queue.start();
    }
}