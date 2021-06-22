package com.example.myapplication.ui.home;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.DataModel;
import com.example.myapplication.MyAdapter;
import com.example.myapplication.PlayService;
import com.example.myapplication.R;
import com.example.myapplication.Send;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static androidx.core.content.ContextCompat.getSystemService;

public class HomeFragment extends Fragment {

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
    boolean done = false;
    public static String path;
    List<String> files1;
    JSONObject cached;
    private HomeViewModel homeViewModel;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        tracksView = root.findViewById(R.id.all_tracks);
        seek = root.findViewById(R.id.seekBar1);
        pause = root.findViewById(R.id.stopPause);
        serviceIntent = new Intent(getContext(), PlayService.class);
        cached = new JSONObject();
        username = "np4";

        tracks = new ArrayList<>();
//        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
////                textView.setText(s);
//            }
//        });

//        if (savedInstanceState != null) {
//            tracksView.setAdapter(new MyAdapter(getContext(), dataModelList));
//        } else {
//
//        }

        tracksView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->{
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
                    getActivity().stopService(serviceIntent);
                }
                getActivity().startService(serviceIntent);
            } else {
                executePost(getResources().getString(R.string.server_ip)+
                        "Serv1?trackname="+trackName+"&username="+"np4","",position);
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
                    PlayService.mediaSession.setPlaybackState(PlayService.getPBS(1.0f,
                            PlayService.m.getCurrentPosition()));
                } else {
                    PlayService.mediaSession.setPlaybackState(PlayService.getPBS(0f,
                            PlayService.m.getCurrentPosition()));
                }

            }
        });

        if (isMyServiceRunning(PlayService.class)) {
            if (!PlayService.m.isPlaying()) {
                getActivity().stopService(serviceIntent);
            } else {
                pause.setEnabled(true);
                seek.setEnabled(true);
                seek.setMax(PlayService.m.getDuration() / 1000);
            }
        }


        path =getActivity().getFilesDir().getPath();
        largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.m);
        path = getActivity().getFilesDir().getPath();

        File directory = new File(path);
        files = directory.listFiles();

        files1 = new ArrayList<>();
        dataModelList = new ArrayList<>();
        Log.d("files", Arrays.toString(files));
        Log.d("url1",path);


        if (files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                try {
                    cached.put(String.valueOf(i), files[i].getName());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            tracks = new ArrayList<>();
            for (File f : files) {
                tracks.add(f.getName());
                mmr = new MediaMetadataRetriever();
                try {
                    mmr.setDataSource(path+"/"+f.getName());
                }catch (Exception ex) {
                    Log.d("mmrEx",ex.toString());
                }
                byte[] img = mmr.getEmbeddedPicture();
                if (img == null) {
                    dataModelList.add(new DataModel(f.getName(), largeIcon));
                } else {
                    addDataModelList(f.getName(), img);
                }
            }

            new Thread(()-> tracksView.setAdapter(new MyAdapter(getContext(), dataModelList))).start();
        }

//        postData("np4");
        sendCachedTracks();

        return root;
    }


    public void sendCachedTracks(){
        String url = "http://192.168.1.108:8080/studentsapp-1.0-SNAPSHOT/Serv2";

        RequestQueue queue = Volley.newRequestQueue(getContext());

        StringRequest commonRequest = new StringRequest(Request.Method.GET, url + "?cached=" + cached +"&username="+"np4", response -> {
            Log.d("Reeeee", String.valueOf(response));
            JSONArray Jtracks = null;
            JSONArray Jcovers = null;
            try {
                JSONObject obj = new JSONObject(response);
                Jtracks = obj.getJSONArray("tracks");
                Jcovers = obj.getJSONArray("covers");
                Log.d("Jtracks", String.valueOf(response));

//                okTracks(tracks, covers);

            } catch (JSONException e) {
                Log.d("TAG", response);
                e.printStackTrace();
            }

            JSONArray finalJtracks = Jtracks;
            JSONArray finalJcovers = Jcovers;
            new Thread(() -> {

                for (int i = 0; i < finalJtracks.length(); i++) {
                    try {
                        tracks.add(finalJtracks.getString(i));
                        if (finalJcovers.getString(i).equals("none")) {
                            dataModelList.add(new DataModel(finalJtracks.getString(i), largeIcon));
                        }
                        else {
                            byte[] img = extractAlbumCoverFromJson(finalJcovers.getString(i));
                            addDataModelList(finalJtracks.getString(i), img);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        tracksView.setAdapter(new MyAdapter(getContext(), dataModelList));
                        for (int i = 0; i < finalJtracks.length(); i++) {
                            try {
                                Log.d("TAG", "data" + finalJtracks.getString(i));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                });

            }).start();



        }, error -> Log.d("vollerr", error.toString())) {
            @Override
            protected Map<String, String> getParams() {

                return null;
            }
        };
        queue.add(commonRequest);
        queue.start();
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

    private void addDataModelList(String fileName, byte[] img) {
        InputStream inputStream = new ByteArrayInputStream(img);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        dataModelList.add(new DataModel(fileName, bitmap));
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void stream2file(InputStream in, int pos)  {

        File file =  new File( path+File.separator +tracks.get(pos));

        new Thread(() -> {
            try (OutputStream outputStream = new FileOutputStream(file)) {
                IOUtils.copy(in, outputStream);
                if (isMyServiceRunning(PlayService.class)) {
                    getActivity().stopService(serviceIntent);
                }
                getActivity().startService(serviceIntent);
            } catch (IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        t.setText(e.toString());
                    }
                });
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
//                getActivity().runOnUiThread(() -> t.setText(e.toString()));
                e.printStackTrace();
                connection.disconnect();
            }
        }).start();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!PlayService.m.isPlaying()) {
            getActivity().stopService(serviceIntent);
        }
    }



//    @Override
//    public void onPause() {
//        super.onPause();
//
//
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        Log.d("pause","onResume");
//        if (tracks != null) {
//            tracksView.setAdapter(new MyAdapter(getContext(), dataModelList));
//        }
//    }
}