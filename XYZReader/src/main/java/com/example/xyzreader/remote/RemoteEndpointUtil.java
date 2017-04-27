package com.example.xyzreader.remote;

import android.app.Application;
import android.app.LauncherActivity;
import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.xyzreader.data.UpdaterService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public  class RemoteEndpointUtil extends AppCompatActivity {
    private static final String TAG = "RemoteEndpointUtil";
    private static final String URL_DATA="https://public-api.wordpress.com/rest/v1.1/sites/techcrunch.com/posts/?pretty=true";
    public static JSONArray array;
    static JSONArray nJson;
    public   RequestQueue requestQueue;
    private RemoteEndpointUtil() {
    }

    public JSONArray fetchJsonArray() {


        StringRequest stringRequest=new StringRequest(com.android.volley.Request.Method.GET, URL_DATA, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String s) {

                try {
                    JSONObject jsonObject = new JSONObject(s);
                    JSONArray array = jsonObject.getJSONArray("posts");
                    setJArray(array);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


            }
        });

       requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);



    return array;

    }

    public void setJArray(JSONArray a)
    {
        nJson=a;
    }

    public static JSONArray getJArray(){

        return nJson;
    }

    static String fetchPlainText(URL url) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
