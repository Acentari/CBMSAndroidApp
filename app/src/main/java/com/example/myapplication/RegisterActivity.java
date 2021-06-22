package com.example.myapplication;

import android.content.Intent;
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
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    EditText email;
    EditText fName;
    EditText lName;
    EditText username;
    EditText password;
    String em;
    String fn;
    String ln;
    String user;
    String pass;
    Button btnReg;
    TextView t;
    String responseUserName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.regiser);
        email = findViewById(R.id.email);
        fName = findViewById(R.id.fname);
        lName = findViewById(R.id.lname);
        username = findViewById(R.id.user);
        password = findViewById(R.id.pass);
        btnReg = findViewById(R.id.reg);
        t = findViewById(R.id.t);


        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                em = email.getText().toString();
                fn = fName.getText().toString();
                ln = lName.getText().toString();
                user = username.getText().toString();
                pass = password.getText().toString();
                postData(em, fn, ln, user, pass);
            }
        });
    }


    public void postData(String email, String fName, String lName, String username, String password) {
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = "http://192.168.1.108:8080/studentsapp-1.0-SNAPSHOT/RegisterServlet";


        Map<String, String> params = new HashMap<>();
        params.put("email", "email");
        params.put("fname", "fName");
        params.put("lname", "lName");
        params.put("username", "username4");
        params.put("password", "password");


        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                response -> {
                    try {
                        responseUserName = response.getString("logedin");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (String.valueOf(responseUserName).equals("ok")){
                        Intent intend = new Intent();
                        intend.putExtra("user",username);
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }

                    else {
                        t.setText(String.valueOf(responseUserName));
                    }

                }, error -> t.setText("error")) {


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
