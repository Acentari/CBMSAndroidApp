package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        SharedPreferences sp1=this.getSharedPreferences("Login", MODE_PRIVATE);
        String username = sp1.getString("username", null);
        Intent intent;
        if (username == null) {
            intent = new Intent(getApplicationContext(), LogInActivity.class);
        } else {
            intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("user", username);
        }
        startActivity(intent);
    }
}