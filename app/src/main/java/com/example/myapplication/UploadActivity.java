package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadActivity extends Activity {
    ListView tracks;
    TextView messageText;
    Button uploadButton;
    int serverResponseCode = 0;
    ProgressDialog dialog = null;
    String username;
    String upLoadServerUri = null;
    Button ch;
    File source;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uploas);
        messageText = findViewById(R.id.upt);
        tracks = findViewById(R.id.songs);
        uploadButton = findViewById(R.id.up1);
        List<String> songs = new ArrayList<>();
        upLoadServerUri = "http://192.168.1.108:8080//studentsapp-1.0-SNAPSHOT/UploadServlet";
        Intent intent = getIntent();
        username = intent.getStringExtra("user");
        int permission = ActivityCompat.checkSelfPermission(UploadActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UploadActivity.this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        String[] projection = {MediaStore.Audio.Media.DATA,};

        Cursor cursor = UploadActivity.this.managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);


        while(cursor.moveToNext()){
            songs.add(cursor.getString(0));
        }
        final ArrayAdapter<String> aAdpt = new ArrayAdapter<>(UploadActivity.this, android.R.layout.simple_list_item_1, songs);

        tracks.setAdapter(aAdpt);
        messageText.setText(username);


        tracks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String path = songs.get(position);
                messageText.setText(path);


                new Thread(new Runnable() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                messageText.setText("uploading started.....");
                            }
                        });

                        source  = new File(path);
                        uploadFile1(source);

                    }
                }).start();
            }
        });


        uploadButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }


    public int uploadFile1(File sourceFile) {
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;

        if (!sourceFile.isFile()) {
//            dialog.dismiss();

            Log.e("uploadFile", "Source File not exist :" + "" + sourceFile.getPath());

            runOnUiThread(new Runnable() {
                public void run() {
                    messageText.setText("Source File not exist :" + "" + sourceFile.getPath());
                }
            });

            return 0;

        }
        else {
            HttpURLConnection conn = null;
            try {
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);

                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name="+username+"; filename="+sourceFile.getName()+lineEnd);
                dos.writeBytes(lineEnd);

                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                fileInputStream.close();
                dos.flush();
                dos.close();

                serverResponseCode = conn.getResponseCode();
                if(serverResponseCode == 200){
                    runOnUiThread(new Runnable() {
                        public void run() {
                            String msg = "File Upload Completed.";
                            messageText.setText(msg);
                        }
                    });
                }


            } catch (MalformedURLException ex) {

//                dialog.dismiss();
                ex.printStackTrace();


            } catch (Exception e) {

//                dialog.dismiss();
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        messageText.setText(e.toString());

                    }
                });
                Log.e( "Exception : " + e.getMessage(), String.valueOf(e));
            }
            return serverResponseCode;
        }
    }
}
//how to set percentage for progreess bar from xml

