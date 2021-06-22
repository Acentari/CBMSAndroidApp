package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogInActivity extends AppCompatActivity {
    EditText username;
    EditText password;
    String user;
    String pass;
    String logedIn;
    Button btnLogIn;
    TextView t;
    String responseUserName ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        username = findViewById(R.id.user);
        password = findViewById(R.id.pass);
        btnLogIn = findViewById(R.id.btnLog);
        t = findViewById(R.id.t);


        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = username.getText().toString();
                pass = password.getText().toString();
                postData(user, pass);
            }
        });
    }

    public void postData(String username, String password) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://192.168.1.108:8080/CBMS/LogInServlet";
        Map<String, String> params = new HashMap<>();
        params.put("username",username);
        params.put("password", password);
        SharedPreferences sp=getSharedPreferences("Login", MODE_PRIVATE);
        SharedPreferences.Editor Ed=sp.edit();
        Ed.putString("username",username);
        Ed.apply();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            responseUserName = response.getString("response");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (String.valueOf(responseUserName).equals("ok")) {
                            t.setText("done");
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra("user", username);
                            startActivity(intent);
                        }

                        else {
                            t.setText("not done");
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        t.setText("error");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };

        queue.add(jsonObjReq);
        queue.start();
    }
}
