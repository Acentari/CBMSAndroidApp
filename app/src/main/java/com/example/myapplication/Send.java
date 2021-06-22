package com.example.myapplication;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class Send {
    Context ctx;
    String url = "http://192.168.1.108:8080/studentsapp-1.0-SNAPSHOT/Serv2";
    JSONObject rObj;

    public Send(Context ctx) {
        this.ctx = ctx;
    }

    public void send(JSONObject obj, String username) {
        RequestQueue queue = Volley.newRequestQueue(ctx);

        StringRequest commonRequest = new StringRequest(Request.Method.GET, url + "?cached=" + obj +"&username="+username, response -> {

            try {
                rObj = new JSONObject(response);
                Log.d("respondedNotCached: ",rObj.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d("vollerr", error.toString())) {
            @Override
            protected Map<String, String> getParams() {

                return null;
            }
        };
        queue.add(commonRequest);
        queue.start();
    }

    public JSONObject getRObj() {
        return rObj;
    }
}
